package com.example.aliussama.fawry.View.User;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.aliussama.fawry.Model.Callbacks.OnAddMachineListener;
import com.example.aliussama.fawry.Model.GPSTracker;
import com.example.aliussama.fawry.Model.MachineModel;
import com.example.aliussama.fawry.Model.UserDatabase;
import com.example.aliussama.fawry.Model.UserModel;
import com.example.aliussama.fawry.R;
import com.example.aliussama.fawry.View.LoginActivity;
import com.example.aliussama.fawry.View.ScannerActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class HomeUserActivity extends AppCompatActivity implements View.OnClickListener,
        OnAddMachineListener, SearchView.OnQueryTextListener {

    final String HOME_USER_TAG = "HomeUserActivity";
    Toolbar toolbar;

    @BindView(R.id.activity_home_user_determinateBar_moreThan_20)
    ProgressBar mProgressBarMoreThanAPI20;

    @BindView(R.id.activity_home_user_determinateBar_lessThan_21)
    ProgressBar mProgressBarLessThanAPI21;

    public static EditText machineCodeEditText;
    EditText clientNameEditText, clientPhoneEditText;
    EditText currentAddressEditText;
    FloatingActionButton addLocationFab, mScanMachineSerialNumberFab;
    Button addMachineButton;

    String mCurrentAddressName;
    public static String mMachineId;

    //declare current location var
    private Location mCurrentLocation;

    //declare Place Pick Builder request code var
    private final int PLACE_PICKER_REQUEST = 1;

    //declare Place Pick Builder reference
//    private Place .IntentBuilder builder;

    PlacesClient placesClient;

    private GPSTracker mGpsTracker;

    private HandlerThread mBackgroundThread;

    private Handler mBackgroundHandler;

    private Handler mChangeUIHandler;

    private UserDatabase mUserDatabase;

    private MachineModel mMachine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_user);
        ButterKnife.bind(this);

        init();
    }

    private void init() {
        try {
            if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

            }
            // Initialize Places.
            Places.initialize(getApplicationContext(), "AIzaSyAAPX5De3aThZZMntG0Gz0wKd9xeGp8aAQ");

            // Create a new Places client instance.
            placesClient = Places.createClient(this);

            //toolbar
            toolbar = findViewById(R.id.activity_home_user_toolbar);
            setSupportActionBar(toolbar);

            machineCodeEditText = findViewById(R.id.activity_home_user_machine_code_edit_text);
            clientNameEditText = findViewById(R.id.activity_home_user_client_name_edit_text);
            clientPhoneEditText = findViewById(R.id.activity_home_user_client_phone_edit_text);

            addLocationFab = findViewById(R.id.activity_home_user_add_location_fab);
            mScanMachineSerialNumberFab = findViewById(R.id.activity_home_user_machine_code_fab);
            addMachineButton = findViewById(R.id.activity_home_user_add_machine_button);
            currentAddressEditText = findViewById(R.id.activity_home_user_add_location_text_view);

            //declaring place builder to pick specific place
//            builder = new PlacePicker.IntentBuilder();
            mGpsTracker = new GPSTracker(this);
            mGpsTracker.getLocation();

            mBackgroundThread = new HandlerThread("HomeUserThread");
            mBackgroundThread.start();
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
            mChangeUIHandler = new Handler(Looper.getMainLooper());

            mUserDatabase = new UserDatabase();

            addMachineButton.setOnClickListener(this);
            addLocationFab.setOnClickListener(this);
            mScanMachineSerialNumberFab.setOnClickListener(this);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.sign_out_user:
                    deleteLocalSharedPreferences(getString(R.string.shared_preferences_file_name));
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    return true;
                case R.id.home_user_search:
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteLocalSharedPreferences(String string) {
        try {
            SharedPreferences mPreferences = getSharedPreferences(string, MODE_PRIVATE);
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(getString(R.string.type_key), getString(R.string.default_value_of_shared_preferences_string));
            editor.apply();
            Log.i(HOME_USER_TAG, "deleteLocalSharedPreferences(): type is None");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.home_user_menu, menu);

        // Get the SearchView and set the searchable configuration
        //declare Search Manager
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //Declare Search View and associate it to it's icon in menu in toolbar
        SearchView searchView = (SearchView) menu.findItem(R.id.home_user_search).getActionView();
        //change Search view EditText TextColor to white
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.white));
        searchEditText.setHintTextColor(getResources().getColor(R.color.white));

        // Assumes current activity is the searchable activity
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(this);
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        try {
            if (R.id.activity_home_user_add_machine_button == view.getId()) {
                addMachineOnClick();
            } else if (R.id.activity_home_user_add_location_fab == view.getId()) {
//                getLatestKnownLocation();
                addCurrentLocationOnClick();
            } else if (R.id.activity_home_user_machine_code_fab == view.getId()) {
                ScanSerialNumber();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLoading() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                mProgressBarMoreThanAPI20.setVisibility(View.VISIBLE);
                mProgressBarLessThanAPI21.setVisibility(View.GONE);
            } else {
                mProgressBarMoreThanAPI20.setVisibility(View.GONE);
                mProgressBarLessThanAPI21.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopLoading() {
        try {
            mProgressBarMoreThanAPI20.setVisibility(View.GONE);
            mProgressBarLessThanAPI21.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(HOME_USER_TAG, "onRequestPermissionsResult(): ACCESS_COARSE_LOCATION is granted");
                mGpsTracker.getLocation();
            } else {
                Log.i(HOME_USER_TAG, "onRequestPermissionsResult(): ACCESS_COARSE_LOCATION is not granted");
            }
        }
    }

    private void ScanSerialNumber() {
        try {
            Intent intent = new Intent(this, ScannerActivity.class);
            intent.putExtra("activity_type", "homeUserActivity");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addCurrentLocationOnClick() {
        try {

            Log.i(HOME_USER_TAG, "addCurrentLocationOnClick(): ACCESS_FINE_LOCATION permission granted");
            //starting Place Picker Builder
            if (mGpsTracker.canGetLocation()) {
                Log.i(HOME_USER_TAG, "addCurrentLocationOnClick(): GPSTracker is enabled");
                mGpsTracker.getLocation();
                LatLng currentLocationLatLng = new LatLng(mGpsTracker.getLatitude(), mGpsTracker.getLongitude());
                Log.i(HOME_USER_TAG, "current location Latitude = " + currentLocationLatLng.latitude);
                Log.i(HOME_USER_TAG, "current location Longitude = " + currentLocationLatLng.longitude);

                LatLngBounds currentLocation = new LatLngBounds(currentLocationLatLng, currentLocationLatLng);
                mCurrentLocation = new Location("");
                mCurrentLocation.setLatitude(currentLocationLatLng.latitude);
                mCurrentLocation.setLongitude(currentLocationLatLng.longitude);
                mCurrentAddressName = "none";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addMachineOnClick() {
        try {
            if (machineCodeEditText.getText() == null || machineCodeEditText.getText().toString().isEmpty()) {
                machineCodeEditText.setError(getString(R.string.required));

            } else if (clientNameEditText.getText() == null || clientNameEditText.getText().toString().isEmpty()) {
                clientNameEditText.setError(getString(R.string.required));

            } else if (clientPhoneEditText.getText() == null || clientPhoneEditText.getText().toString().isEmpty()) {
                clientPhoneEditText.setError(getString(R.string.required));

            } else if (mCurrentLocation == null) {
                Toast.makeText(this, getString(R.string.please_choose_your_current_location), Toast.LENGTH_SHORT).show();

            } else if (currentAddressEditText.getText() == null || currentAddressEditText.getText().toString().isEmpty()) {
                currentAddressEditText.setError("مطلوب");
            } else {
                showLoading();
                mMachineId = machineCodeEditText.getText().toString();
                String clientName = clientNameEditText.getText().toString();
                String clientPhone = clientPhoneEditText.getText().toString();
                String currentLocationLatitude = String.valueOf(mCurrentLocation.getLatitude());
                String currentLocationLongitude = String.valueOf(mCurrentLocation.getLongitude());
                String mCurrentAddressName = currentAddressEditText.getText().toString();
                String userName = getUsername();
                String userID = getUserID();
                mMachine = new MachineModel(
                        mMachineId,
                        clientName,
                        clientPhone,
                        mCurrentAddressName,
                        currentLocationLatitude,
                        currentLocationLongitude,
                        userName,
                        userID);

                mBackgroundHandler.post(() -> {
                    try {
                        mUserDatabase.checkIfMachineExists(mMachineId, HomeUserActivity.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onMachineExists(boolean status) {
        try {
            if (!status) { // if machine doesn't exists in database
                if (mBackgroundThread.isAlive() && mBackgroundHandler != null) {
                    mBackgroundHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (mUserDatabase != null) {
                                    showLoading();
                                    UserModel userModel = getUserModel();
                                    mUserDatabase.addMachine(mMachine, userModel, HomeUserActivity.this);

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        private UserModel getUserModel() {
                            try {
                                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_file_name), MODE_PRIVATE);
                                String username = sharedPreferences.getString(getString(R.string.username), getString(R.string.default_value_of_shared_preferences_string));
                                String type = sharedPreferences.getString(getString(R.string.type_key), getString(R.string.default_value_of_shared_preferences_string));
                                String phone = getString(R.string.default_value_of_shared_preferences_string);
                                String id = sharedPreferences.getString(getString(R.string.user_id), getString(R.string.default_value_of_shared_preferences_string));

                                return new UserModel(id, username, phone, type);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    });
                }
            } else {// if machine exists in database
                mChangeUIHandler.post(() -> Toast.makeText(HomeUserActivity.this, getString(R.string.machineAlreadyExists), Toast.LENGTH_LONG).show());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAddMachineSuccess(boolean status) {
        Log.i("onAddMachineSuccess", "status is " + status);

        stopLoading();

        if (mChangeUIHandler != null)
            mChangeUIHandler.post(() -> {
                try {
                    Log.i("mChangeUIHandler", "stopping loading");
                    Log.i("mChangeUIHandler", "displaying toast");

                    machineCodeEditText.setText("");
                    clientNameEditText.setText("");
                    clientPhoneEditText.setText("");
                    currentAddressEditText.setText("");
                    mMachine = null;
                    mCurrentAddressName = null;

                    Toast.makeText(HomeUserActivity.this, getString(R.string.machineAddedSuccessfully), Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }

    @Override
    public void onAddMachineFailure(Exception e) {
        Log.i("onAddMachineFailure", e.getMessage());
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        try {
            Intent intent = new Intent(this, UserSearchActivity.class);
            intent.putExtra("query", query);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private String getUsername() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_file_name), MODE_PRIVATE);
        String username = "none";
        try {
            username = sharedPreferences.getString(getString(R.string.username), "none");
            Log.i(HOME_USER_TAG, "Current User is : " + username);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return username;
    }

    private String getUserID() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_file_name), MODE_PRIVATE);
        String userID = "none";
        try {
            userID = sharedPreferences.getString(getString(R.string.user_id), "none");
            Log.i(HOME_USER_TAG, "Current User ID is : " + userID);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return userID;
    }

}
