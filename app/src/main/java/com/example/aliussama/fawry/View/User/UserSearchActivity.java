package com.example.aliussama.fawry.View.User;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aliussama.fawry.Model.Callbacks.ReadingAllDatabaseCallback;
import com.example.aliussama.fawry.Model.Callbacks.SearchActivityCallback;
import com.example.aliussama.fawry.Model.MachineModel;
import com.example.aliussama.fawry.Model.UserDatabase;
import com.example.aliussama.fawry.Model.UserModel;
import com.example.aliussama.fawry.R;
import com.example.aliussama.fawry.View.Admin.AllMachinesRecAdapter;
import com.example.aliussama.fawry.View.Admin.SearchActivity;
import com.example.aliussama.fawry.View.LoginActivity;
//import com.google.android.gms.location.places.Place;
//import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;

public class UserSearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        ReadingAllDatabaseCallback,
        SearchActivityCallback {

    final String TAG = "UserSearchActivity";
    final String HANDLER_THREAD_NAME = "SearchActivityThread";
    //declare Place Pick Builder request code var
    private int PLACE_PICKER_REQUEST = 1;

    private final String USER = "USER";

    String Query;

    Toolbar toolbar;
    SearchView searchView;
    SearchManager searchManager;
    EditText searchEditText;
    ProgressBar mProgressBarMoreThanAPI20;

    HandlerThread mThread;
    Handler mBackgroundHandler;
    Handler mChangeUIHandler;

    UserDatabase mUserDatabase;

    RecyclerView mMachinesRecyclerView;
    ArrayList<MachineModel> machines;

    AllMachinesRecAdapter mAllMachinesRecAdapter;

    ConstraintLayout machinesLayout;

    String currentUsername;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_user_search);
            Query = getIntent().getStringExtra("query");
            init();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        try {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);


            }

            //initialize current username
            currentUsername = getUsername();

            //toolbar
            toolbar = findViewById(R.id.activity_search_toolbar);
            setSupportActionBar(toolbar);

            //Progress Bar
            mProgressBarMoreThanAPI20 = findViewById(R.id.activity_search_determinateBar_moreThan_20);

            mUserDatabase = new UserDatabase();

            //Threads & Handlers
            mThread = new HandlerThread(HANDLER_THREAD_NAME);
            mThread.start();
            mBackgroundHandler = new Handler(mThread.getLooper());
            mChangeUIHandler = new Handler(Looper.getMainLooper());

            machinesLayout = findViewById(R.id.activity_search_users_constraints_layout2);

            //Machines RecyclerView
            machines = new ArrayList<>();
            mMachinesRecyclerView = findViewById(R.id.activity_search_machines_recycler_view);
            mMachinesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mAllMachinesRecAdapter = new AllMachinesRecAdapter(machines, UserSearchActivity.this, USER);
            mMachinesRecyclerView.setAdapter(mAllMachinesRecAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            //reading all users and machines in database
            if (mBackgroundHandler != null) {

                mProgressBarMoreThanAPI20.setVisibility(View.VISIBLE);

                mBackgroundHandler.post(() -> {
                    try {
                        if (mUserDatabase != null) {
                            mUserDatabase.getUserMachines(getUserID(),UserSearchActivity.this);
                        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.home_user_menu, menu);

        // Get the SearchView and set the searchable configuration
        //declare Search Manager
        searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //Declare Search View and associate it to it's icon in menu in toolbar
        searchView = (SearchView) menu.findItem(R.id.home_user_search).getActionView();

        //change Search view EditText TextColor to white
        searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.white));
        searchEditText.setHintTextColor(getResources().getColor(R.color.white));

        // Assumes current activity is the searchable activity
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setSubmitButtonEnabled(true);
            searchView.setOnQueryTextListener(this);
        }
        return true;
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
            Log.i(TAG, "deleteLocalSharedPreferences(): type is None");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        try {
            if (query != null && !query.isEmpty()) {
                ArrayList<MachineModel> searchResult = new ArrayList<>();
                for (MachineModel machine : machines) {
                    if (machine.getmClientName().toLowerCase().contains(query.toLowerCase()) ||
                            machine.getmMachineId().toLowerCase().contains(query.toLowerCase()) ||
                            machine.getmClientPhone().toLowerCase().contains(query.toLowerCase()) ||
                            machine.getmAddress().toLowerCase().contains(query.toLowerCase()) ||
                            machine.getmRepresentativeName().toLowerCase().contains(query.toLowerCase())) {
                        searchResult.add(machine);
                    }
                }
                mAllMachinesRecAdapter.NotifyAdapter(searchResult);
                Log.i("onAllMachinesSuccess", "Machines Adapter has been notified");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        try {
            if (newText != null && !newText.isEmpty()) {


                //Search in Machines List
                ArrayList<MachineModel> machinesSearchResult = new ArrayList<>();
                Log.i("onQueryTextChange", "machines Size = " + machines.size());
                for (MachineModel machine : machines) {
                    Log.i("onQueryTextChange", "current client name" + machine.getmClientName());

                    if (machine.getmClientName().toLowerCase().contains(newText.toLowerCase()) ||
                            machine.getmMachineId().toLowerCase().contains(newText.toLowerCase()) ||
                            machine.getmClientPhone().toLowerCase().contains(newText.toLowerCase()) ||
                            machine.getmAddress().toLowerCase().contains(newText.toLowerCase()) ||
                            machine.getmRepresentativeName().toLowerCase().contains(newText.toLowerCase())) {
                        machinesSearchResult.add(machine);
                    }
                }

                // if search result list contains any result
                if (machinesSearchResult.size() > 0) {
                    //set machines layout's visibility VISIBLE
                    machinesLayout.setVisibility(View.VISIBLE);
                    //Notify machines recyclerView Adapter with search result list
                    mAllMachinesRecAdapter.NotifyAdapter(machinesSearchResult);
                    Log.i("onQueryTextChange", "Machines Adapter has been notified");
                } else {
                    //set machines layout's visibility GONE
                    machinesLayout.setVisibility(View.GONE);
                    Log.i("onQueryTextChange", "No machines matches the search query");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {

            Log.i(TAG, "onActivityResult");
            // handle result of place picker builder
            // if request code equals PLACE_PICKER_REQUEST equals 1
            if (requestCode == PLACE_PICKER_REQUEST) {

                if (resultCode == RESULT_OK) {
                    //getting picked place from the returned Intent
//                    Place place = PlacePicker.getPlace(data, this);
//                    int position = 0;
//                    try {
//                        SharedPreferences sharedPreferences = getSharedPreferences("AllMachinesRecAdapterPosition", Context.MODE_PRIVATE);
//                        Log.i(TAG, "read position from sharedPreferences");
//                        position = sharedPreferences.getInt("position", 0);
//                        Log.i(TAG, "position has been read from sharedPreferences = " + position);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    //if place in not null
//                    if (place != null) {
//                        Log.i(TAG, "onActivityResult : " + place.getAddress());
//                        //handle returned place
//                        Log.i(TAG, "current view holder position = " + position);
//                        Log.i(TAG, "current view holder machine ID = " + ((AllMachinesRecAdapter.viewHolder) mMachinesRecyclerView.findViewHolderForLayoutPosition(position)).machineID.getText().toString());
//
//                        mAllMachinesRecAdapter.onPlaceSelected(place, ((AllMachinesRecAdapter.viewHolder) mMachinesRecyclerView.findViewHolderForLayoutPosition(position)));
//                    } else {
//                        Log.i(TAG, "onActivityResult : place is null");
//
//                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "WRITE_EXTERNAL_STORAGE is granted");
            } else {
                Log.i(TAG, "WRITE_EXTERNAL_STORAGE is not granted");
            }
        }
    }

    //---------------------------------Callbacks---------------------------------------------------

    @Override
    public void onAllUsersSuccess(ArrayList<UserModel> users) {

    }

    @Override
    public void onAllUsersFailure(Exception e) {

    }

    @Override
    public void onAllMachinesSuccess(final ArrayList<MachineModel> mMachines) {
        try {
            //handle if machines are not null
            if (machines != null) {
                //posting runnable on MainThread Handler
                // to change UI Machines RecyclerView
                mChangeUIHandler.post(() -> {
                    try {
                        Log.i("onAllUsersSuccess", "Assigning mUsers to users");

                        mProgressBarMoreThanAPI20.setVisibility(View.GONE);

                        //Assigning machines returned from database
                        //to the machines adapter list
                        machines = mMachines;
                        //handle if there is query
                        if (Query != null && !Query.isEmpty()) {
                            //declaring arrayList of machineModel
                            //to hold search result
                            ArrayList<MachineModel> searchResult = new ArrayList<>();
                            //looping over machines to search for the query
                            for (MachineModel machine : machines) {
                                //check if current machine item contains
                                //current query
                                if ((machine.getmClientName().toLowerCase().contains(Query.toLowerCase()) ||
                                        machine.getmMachineId().toLowerCase().contains(Query.toLowerCase()) ||
                                        machine.getmClientPhone().toLowerCase().contains(Query.toLowerCase()) ||
                                        machine.getmAddress().toLowerCase().contains(Query.toLowerCase())) && machine.getmRepresentativeName().toLowerCase().matches(currentUsername)) {
                                    //If so, add current machine to search result list
                                    searchResult.add(machine);
                                }
                            }
                            // if search result list contains any result
                            if (searchResult.size() > 0) {
                                //set machines layout's visibility VISIBLE
                                machinesLayout.setVisibility(View.VISIBLE);
                                //Notify machines recyclerView Adapter with search result list
                                mAllMachinesRecAdapter.NotifyAdapter(searchResult);
                                Log.i("onAllMachinesSuccess", "Machines Adapter has been notified");
                            } else {
                                //set machines layout's visibility GONE
                                machinesLayout.setVisibility(View.GONE);
                                Log.i("onAllMachinesSuccess", "No machines matches the search query");

                            }
                        }
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
    public void onAllMachinesFailure(String message) {

        mProgressBarMoreThanAPI20.setVisibility(View.GONE);

        Log.i("SearchActivity", "" + message);
    }

    @Override
    public void onUserItemDelete(UserModel user) {

    }

    @Override
    public void onUserItemUpdate(UserModel user) {

    }

    @Override
    public void onMachineItemDelete(final MachineModel machine) {
        try {
            Log.i(TAG,"onMachineItemDelete() is called");
            if (mThread == null || mBackgroundHandler == null) {
                Log.i(TAG,"onMachineItemDelete(): initializing Thread");

                mThread = new HandlerThread(HANDLER_THREAD_NAME);
                mBackgroundHandler = new Handler(mThread.getLooper());
            }
            if (mUserDatabase == null) {
                Log.i(TAG,"onMachineItemDelete(): intializing userDatabase Object");
                mUserDatabase = new UserDatabase();
            }
            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG,"onMachineItemDelete(): calling deleteMachine in a background thread with machineUID = "+ machine.getmUID());

                        mUserDatabase.deleteMachine(machine, UserSearchActivity.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMachineItemUpdate(MachineModel machine) {

    }

    @Override
    public void onMachineDeletedSuccess(final boolean status) {
        try {
            if (mChangeUIHandler == null)
                mChangeUIHandler = new Handler(Looper.getMainLooper());
            mChangeUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (status) {
                        Toast.makeText(UserSearchActivity.this, getString(R.string.machine_deleted_successfully), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(UserSearchActivity.this, getString(R.string.error_message_to_user), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMachineDeleteFailure(Exception e) {

    }

    @Override
    public void onMachineUpdatedSuccess(boolean status) {

    }

    @Override
    public void onMachineUpdatedFailure(Exception e) {

    }

    private String getUsername(){
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_file_name),MODE_PRIVATE);
        String username = "none";
        try {
            username = sharedPreferences.getString(getString(R.string.username), "none");
            Log.i(TAG,"Current User is : "+username);

        }catch (Exception e){
            e.printStackTrace();
        }
        return username;
    }

    private String getUserID(){
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_file_name),MODE_PRIVATE);
        String userID = "none";
        try {
            userID = sharedPreferences.getString(getString(R.string.user_id), "none");
            Log.i(TAG,"Current User ID is : "+userID);

        }catch (Exception e){
            e.printStackTrace();
        }
        return userID;
    }

}
