package com.example.aliussama.fawry.View.Admin;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.aliussama.fawry.Model.Callbacks.ReadingAllDatabaseCallback;
import com.example.aliussama.fawry.Model.MachineModel;
import com.example.aliussama.fawry.View.LoginActivity;
import com.example.aliussama.fawry.Model.Callbacks.UserDatabaseCallback;
import com.example.aliussama.fawry.Model.UserDatabase;
import com.example.aliussama.fawry.Model.UserModel;
import com.example.aliussama.fawry.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeAdminActivity extends AppCompatActivity implements UserDatabaseCallback,
        SearchView.OnQueryTextListener, ReadingAllDatabaseCallback {

    final String HANDLER_THREAD_NAME = "HomeAdminActivityThread";
    final String TAG = "HomeAdminActivity";
    Toolbar toolbar;
    SearchView searchView;
    SearchManager searchManager;
    EditText searchEditText;
    ProgressBar mProgressBarMoreThanAPI20;
    EditText usernameEditText, phoneEditText;
    UserDatabase mUserDatabase;
    UserModel user;
    HandlerThread mThread;
    Handler mBackgroundHandler;
    Handler mChangeUIHandler;
    String userCode = "";
    ArrayList<MachineModel> machines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_admin);

        init();
    }

    private void init() {
        try {

            //toolbar
            toolbar = findViewById(R.id.activity_home_admin_toolbar);
            setSupportActionBar(toolbar);

            //Views
            usernameEditText = findViewById(R.id.activity_home_admin_user_name_edit_text);
            phoneEditText = findViewById(R.id.activity_home_admin_user_phone_edit_text);
            mProgressBarMoreThanAPI20 = findViewById(R.id.activity_home_admin_determinateBar_moreThan_20);

            mThread = new HandlerThread(HANDLER_THREAD_NAME);
            mThread.start();
            mBackgroundHandler = new Handler(mThread.getLooper());
            mChangeUIHandler = new Handler(Looper.getMainLooper());

            mUserDatabase = new UserDatabase();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home_admin_search:
                return true;
            case R.id.home_admin_csv_report:
                readAllMachines();
                return true;
            case R.id.sign_out:
                try {
                    deleteLocalSharedPreferences(getString(R.string.shared_preferences_file_name));
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        String TAG = "Permission : ";
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            exportMachinesIntoCSV(machines);

        }
    }

    private void readAllMachines() {
        try {
            mUserDatabase.getAllMachines(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public File getPublicAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStorageDirectory(), albumName);
        Log.i(TAG, file.getPath());


        if (!file.mkdirs()) {
            Log.i(TAG, "Directory not found");
            if (file.mkdir()) {
                Log.i(TAG, "Directory created");
            } else {
                Log.i(TAG, "Directory not created");
            }
        }
        return file;
    }

    private void exportMachinesIntoCSV(ArrayList<MachineModel> machines) {

        handleStoragePermissions();

        if (isExternalStorageWritable()) {
            File fileDir;
            BufferedWriter bfWriter = null;

            fileDir = getPublicAlbumStorageDir("Fawry");

            try {
                System.setProperty("file.encoding", "65001");
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Log.i(TAG, "Path " + fileDir.getPath());

                String report_name = createReportName();

                File file = new File(fileDir.getPath() + report_name);
                if (!file.exists()) {
                    try {
                        boolean f = file.createNewFile();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                FileWriter fileWriter = new FileWriter(file);

                Log.i(TAG, "Encoding " + fileWriter.getEncoding());
                bfWriter = new BufferedWriter(fileWriter);

                bfWriter.write("Machine ID,Client Name,Client Phone,Address, Latitude, Longitude\n");

                for (MachineModel machine : machines) {
                    machine.setmAddress(machine.getmAddress().replaceAll(",", " - "));
                    String seperator = ",";
                    bfWriter.write(machine.getmMachineId() + seperator + machine.getmClientName() + seperator + machine.getmClientPhone() + seperator + machine.getmAddress() + seperator + machine.getmLatitude() + seperator + machine.getmLongitude() + "\n");
                }

                bfWriter.close();

                Toast.makeText(this, getString(R.string.report_saved), Toast.LENGTH_SHORT).show();

                try{
                    Intent mailIntent = new Intent(Intent.ACTION_SEND);
                    startActivity(mailIntent);

                }catch(Exception exc){
                    exc.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();

                try {
                    if (bfWriter != null)
                        bfWriter.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.no_storage_available), Toast.LENGTH_SHORT).show();
        }
    }

    private String createReportName() {
        String reportName = "";
        String currentDate = getCurrentDate();
        try {
            int currentVersion = getDatabaseVersion();
            reportName = "machine-" + currentDate + "-FA0" + currentVersion + ".csv";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reportName;
    }

    private int getDatabaseVersion() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.database_version), MODE_PRIVATE);
        int currentVersion = sharedPreferences.getInt(getString(R.string.database_version), 0);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.database_version), ++currentVersion);
        editor.apply();
        return currentVersion;
    }

    private String getCurrentDate() {
        String formattedDate = "";
        try {
            Date c = Calendar.getInstance().getTime();

            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            formattedDate = df.format(c);
            System.out.println("Current time => " + c);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return formattedDate;
    }

    private void handleStoragePermissions() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission is granted");

                } else {
                    Log.i(TAG, "Permission is revoked");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            } else { //permission is automatically granted on sdk<23 upon installation
                Log.i(TAG, "Permission is granted");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            searchView.setOnQueryTextListener(this);
        }
        return true;
    }

    public void onAddUserClickListener(View view) {
        switch (view.getId()) {
            case R.id.activity_home_admin_add_user_button:
                addUserAction();
                 break;
        }
    }

    private void addUserAction() {
        try {

            mProgressBarMoreThanAPI20.setVisibility(View.VISIBLE);

            if (usernameEditText.getText().toString().isEmpty()) {
                usernameEditText.setError(getResources().getString(R.string.enter_username));

            } else if (phoneEditText.getText().toString().isEmpty()) {
                phoneEditText.setError(getResources().getString(R.string.enter_user_phone));

            } else {

                final String phoneNumber = phoneEditText.getText().toString();
                final String id = usernameEditText.getText().toString();
                final String username = usernameEditText.getText().toString();

                if (userCode != null && !userCode.isEmpty() && phoneNumber.matches(userCode)) {

                    user = new UserModel(id, username, phoneNumber, "user");

                    mBackgroundHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mUserDatabase.CheckIfUserExists(phoneNumber, username, HomeAdminActivity.this);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } else if (userCode == null) {

                    Toast.makeText(this, "من فضلك أنشئ الكود الخاس بالمستخدم", Toast.LENGTH_SHORT).show();

                } else if (!phoneNumber.matches(userCode)) {

                    Toast.makeText(this, "الكود لا يتماثل مع البيانات المدخله، من فضلك انشئ الكود", Toast.LENGTH_SHORT).show();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoginSuccess(boolean state, String Type) {
        try {
            if (!state) {
                //add user to database
                mBackgroundHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mUserDatabase.addUser(user, HomeAdminActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
            } else {
                mChangeUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(HomeAdminActivity.this, "هذا المستخدم موجود بالفعل", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAddUserSuccess(boolean state) {
        if (state) {
            mChangeUIHandler.post(new Runnable() {
                @Override
                public void run() {

                    mProgressBarMoreThanAPI20.setVisibility(View.GONE);

                    Toast.makeText(HomeAdminActivity.this, "تم اضافة المستخدم بنجاح", Toast.LENGTH_SHORT).show();
                    usernameEditText.setText("");
                    phoneEditText.setText("");
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChangeUIHandler != null) {
            mChangeUIHandler.removeCallbacks(null);
            mChangeUIHandler = null;
        }
        if (mBackgroundHandler != null) {
            mBackgroundHandler.removeCallbacks(null);
            mBackgroundHandler = null;
        }
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("query", query);
        startActivity(intent);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        return false;
    }

    //-----------------------------------Callbacks------------------------------------------------
    @Override
    public void onAllUsersSuccess(ArrayList<UserModel> users) {

    }

    @Override
    public void onAllUsersFailure(Exception e) {

    }

    @Override
    public void onAllMachinesSuccess(ArrayList<MachineModel> mMachines) {
        try {
            machines = mMachines;
            exportMachinesIntoCSV(machines);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAllMachinesFailure(String message) {
        Log.i(TAG, message);
    }
}
