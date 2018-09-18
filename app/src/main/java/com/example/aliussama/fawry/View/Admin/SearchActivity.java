package com.example.aliussama.fawry.View.Admin;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.aliussama.fawry.View.LoginActivity;
import com.example.aliussama.fawry.Model.Callbacks.ReadingAllDatabaseCallback;
import com.example.aliussama.fawry.Model.Callbacks.SearchActivityCallback;
import com.example.aliussama.fawry.Model.MachineModel;
import com.example.aliussama.fawry.Model.UserDatabase;
import com.example.aliussama.fawry.Model.UserModel;
import com.example.aliussama.fawry.R;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        ReadingAllDatabaseCallback,
        SearchActivityCallback {
    final String TAG = "SearchActivity";
    final String HANDLER_THREAD_NAME = "SearchActivityThread";
    //declare Place Pick Builder request code var
    private int PLACE_PICKER_REQUEST = 1;

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
    RecyclerView mUsersRecyclerView, mMachinesRecyclerView;
    ArrayList<UserModel> users;
    ArrayList<MachineModel> machines;

    AllUsersRecAdapter mAllUsersRecAdapter;
    AllMachinesRecAdapter mAllMachinesRecAdapter;

    ConstraintLayout usersLayout, machinesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        try {
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
            //toolbar
            toolbar = findViewById(R.id.activity_search_toolbar);
            setSupportActionBar(toolbar);

            //Progress Bar
            mProgressBarMoreThanAPI20 = findViewById(R.id.activity_search_determinateBar_moreThan_20);

            //Threads & Handlers
            mThread = new HandlerThread(HANDLER_THREAD_NAME);
            mThread.start();
            mBackgroundHandler = new Handler(mThread.getLooper());
            mChangeUIHandler = new Handler(Looper.getMainLooper());

            usersLayout = findViewById(R.id.activity_search_users_constraints_layout1);
            machinesLayout = findViewById(R.id.activity_search_users_constraints_layout2);

            mUserDatabase = new UserDatabase();

            //Users RecyclerView
            users = new ArrayList<>();
            mUsersRecyclerView = findViewById(R.id.activity_search_users_recycler_view);
            mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mAllUsersRecAdapter = new AllUsersRecAdapter(users, this, this);
            mUsersRecyclerView.setAdapter(mAllUsersRecAdapter);

            //Machines RecyclerView
            machines = new ArrayList<>();
            mMachinesRecyclerView = findViewById(R.id.activity_search_machines_recycler_view);
            mMachinesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mAllMachinesRecAdapter = new AllMachinesRecAdapter(machines, this);
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

                mBackgroundHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (mUserDatabase != null) {
                                mUserDatabase.getAllUsers(SearchActivity.this);
                                mUserDatabase.getAllMachines(SearchActivity.this);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
        menuInflater.inflate(R.menu.home_admin_menu, menu);

        // Get the SearchView and set the searchable configuration
        //declare Search Manager
        searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //Declare Search View and associate it to it's icon in menu in toolbar
        searchView = (SearchView) menu.findItem(R.id.home_admin_search).getActionView();

        //change Search view EditText TextColor to white
        searchEditText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
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
                ArrayList<UserModel> searchResult = new ArrayList<>();
                for (UserModel user : users) {
                    if (user.getName().toLowerCase().contains(query.toLowerCase()) || user.getEmail().toLowerCase().contains(query.toLowerCase())) {
                        searchResult.add(user);
                    }
                }
                mAllUsersRecAdapter.NotifyAdapter(searchResult);
                Log.i("onAllUsersSuccess", "Users Adapter has been notified");
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

                //Search in Users List
                ArrayList<UserModel> usersSearchResult = new ArrayList<>();
                for (UserModel user : users) {
                    if (user.getName().toLowerCase().contains(newText.toLowerCase()) || user.getEmail().toLowerCase().contains(newText.toLowerCase())) {
                        usersSearchResult.add(user);
                    }
                }
                if (usersSearchResult.size() > 0) {
                    usersLayout.setVisibility(View.VISIBLE);
                    mAllUsersRecAdapter.NotifyAdapter(usersSearchResult);
                } else {
                    usersLayout.setVisibility(View.GONE);
                    Log.i("onQueryTextChange", "No users matches the search query");
                }

                Log.i("onQueryTextChange", "Users Adapter has been notified");

                //Search in Machines List
                ArrayList<MachineModel> machinesSearchResult = new ArrayList<>();
                Log.i("onQueryTextChange", "machines Size = " + machines.size());
                for (MachineModel machine : machines) {
                    Log.i("onQueryTextChange", "current client name" + machine.getmClientName());

                    if (machine.getmClientName().toLowerCase().contains(newText.toLowerCase()) ||
                            machine.getmMachineId().toLowerCase().contains(newText.toLowerCase()) ||
                            machine.getmClientPhone().toLowerCase().contains(newText.toLowerCase()) ||
                            machine.getmAddress().toLowerCase().contains(newText.toLowerCase())) {
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

            Log.i("SearchActivity", "onActivityResult");
            // handle result of place picker builder
            // if request code equals PLACE_PICKER_REQUEST equals 1
            if (requestCode == PLACE_PICKER_REQUEST) {

                if (resultCode == RESULT_OK) {
                    //getting picked place from the returned Intent
                    Place place = PlacePicker.getPlace(data, this);
                    int position = 0;
                    try {
                        SharedPreferences sharedPreferences = getSharedPreferences("AllMachinesRecAdapterPosition", Context.MODE_PRIVATE);
                        Log.i("SearchActivity", "read position from sharedPreferences");
                        position = sharedPreferences.getInt("position", 0);
                        Log.i("SearchActivity", "position has been read from sharedPreferences = " + position);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //if place in not null
                    if (place != null) {
                        Log.i("SearchActivity", "onActivityResult : " + place.getAddress());
                        //handle returned place
                        Log.i("SearchActivity", "current view holder position = " + position);
                        Log.i("SearchActivity", "current view holder machine ID = " + ((AllMachinesRecAdapter.viewHolder) mMachinesRecyclerView.findViewHolderForLayoutPosition(position)).machineID.getText().toString());

                        mAllMachinesRecAdapter.onPlaceSelected(place, ((AllMachinesRecAdapter.viewHolder) mMachinesRecyclerView.findViewHolderForLayoutPosition(position)));
                    } else {
                        Log.i("SearchActivity", "onActivityResult : place is null");

                    }
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

    //----------------------------------------------------------------------------------------------
    //Callbacks
    @Override
    public void onAllUsersSuccess(final ArrayList<UserModel> mUsers) {
        try {
            if (mUsers != null) {
                mChangeUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.i("onAllUsersSuccess", "Assigning mUsers to users");

                            mProgressBarMoreThanAPI20.setVisibility(View.GONE);


                            users = mUsers;
                            if (Query != null && !Query.isEmpty()) {
                                ArrayList<UserModel> searchResult = new ArrayList<>();
                                for (UserModel user : users) {
                                    if (user.getName().contains(Query) || user.getEmail().contains(Query)) {
                                        searchResult.add(user);
                                    }
                                }
                                if (searchResult.size() > 0) {
                                    usersLayout.setVisibility(View.VISIBLE);
                                    mAllUsersRecAdapter.NotifyAdapter(searchResult);
                                    Log.i("onAllUsersSuccess", "Users Adapter has been notified");

                                } else {
                                    usersLayout.setVisibility(View.GONE);
                                    Log.i("onAllUsersSuccess", "No users matches the search query");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAllUsersFailure(Exception e) {

        mProgressBarMoreThanAPI20.setVisibility(View.GONE);

        e.printStackTrace();
    }

    @Override
    public void onAllMachinesSuccess(final ArrayList<MachineModel> mMachines) {
        try {
            //handle if machines are not null
            if (machines != null) {
                //posting runnable on MainThread Handler
                // to change UI Machines RecyclerView
                mChangeUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
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
                                    if (machine.getmClientName().toLowerCase().contains(Query.toLowerCase()) ||
                                            machine.getmMachineId().toLowerCase().contains(Query.toLowerCase()) ||
                                            machine.getmClientPhone().toLowerCase().contains(Query.toLowerCase()) ||
                                            machine.getmAddress().toLowerCase().contains(Query.toLowerCase())) {
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
    public void onUserItemDelete(final UserModel user) {
        if (mUserDatabase == null)
            mUserDatabase = new UserDatabase();
        if (mBackgroundHandler != null) {
            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mUserDatabase.deleteUser(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    @Override
    public void onUserItemUpdate(final UserModel user) {
        if (mUserDatabase == null)
            mUserDatabase = new UserDatabase();
        if (mBackgroundHandler != null) {
            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mUserDatabase.updateUser(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }

    @Override
    public void onMachineItemDelete(final MachineModel machine) {
        try {
            if (mThread == null || mBackgroundHandler == null) {
                mThread = new HandlerThread(HANDLER_THREAD_NAME);
                mBackgroundHandler = new Handler(mThread.getLooper());
            }
            if (mUserDatabase == null) {
                mUserDatabase = new UserDatabase();
            }
            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mUserDatabase.deleteMachine(machine, SearchActivity.this);
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
    public void onMachineDeletedSuccess(final boolean status) {
        try {
            if (mChangeUIHandler == null)
                mChangeUIHandler = new Handler(Looper.getMainLooper());
            mChangeUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (status) {
                        Toast.makeText(SearchActivity.this, getString(R.string.machine_deleted_successfully), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SearchActivity.this, getString(R.string.error_message_to_user), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMachineDeleteFailure(Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onMachineItemUpdate(final MachineModel machine) {
        try {
            if (mThread == null || mBackgroundHandler == null) {
                mThread = new HandlerThread(HANDLER_THREAD_NAME);
                mBackgroundHandler = new Handler(mThread.getLooper());
            }
            if (mUserDatabase == null) {
                mUserDatabase = new UserDatabase();
            }
            Log.i("onMachineItemUpdate", "machine ID" + machine.getmMachineId());

            mBackgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i("mBackgroundThread", "machine ID" + machine.getmMachineId());
                        mUserDatabase.updateMachine(machine, SearchActivity.this);
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
    public void onMachineUpdatedSuccess(final boolean status) {
        try {
            if (mChangeUIHandler == null)
                mChangeUIHandler = new Handler(Looper.getMainLooper());
            mChangeUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (status) {
                        Toast.makeText(SearchActivity.this, getString(R.string.machine_updated_successfully), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SearchActivity.this, getString(R.string.error_message_to_user), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMachineUpdatedFailure(Exception e) {
        e.printStackTrace();
    }
}
