package com.example.aliussama.fawry.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aliussama.fawry.Model.Callbacks.UserDatabaseCallback;
import com.example.aliussama.fawry.Model.UserDatabase;
import com.example.aliussama.fawry.R;
import com.example.aliussama.fawry.View.Admin.HomeAdminActivity;
import com.example.aliussama.fawry.View.User.HomeUserActivity;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,
        UserDatabaseCallback {

    final String HANDLER_THREAD_NAME = "LoginActivityThread";
    final String LOGIN_ACTIVITY_TAG = "LoginActivity";
    final String UPDATE_UI_TAG = "updateUI";
    final String ON_CREATE_TAG = "onCreate";

    public static String mScan_Text_Result;

    EditText CodeEditText, EmailEditText;
    Button LoginButton;
    ProgressBar mProgressBarMoreThanAPI20, mProgressBarLessThanAPI21;
    String mPhoneNumber, mUsername;

    UserDatabase mUserDatabase;
    HandlerThread mThread;
    Handler mBackgroundHandler;
    Handler mChangeUIHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_login);
            Init();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Init() {
        try {

            CodeEditText = findViewById(R.id.activity_login_CodeEditText);

            EmailEditText = findViewById(R.id.activity_login_EmailEditText);

            LoginButton = findViewById(R.id.activity_login_loginButton);
            LoginButton.setOnClickListener(this);

            mProgressBarMoreThanAPI20 = findViewById(R.id.activity_login_determinateBar_moreThan_20);

            mProgressBarLessThanAPI21 = findViewById(R.id.activity_login_determinateBar_lessThan_21);

            mUserDatabase = new UserDatabase();

            mThread = new HandlerThread(HANDLER_THREAD_NAME);
            mThread.start();
            mBackgroundHandler = new Handler(mThread.getLooper());
            mChangeUIHandler = new Handler(Looper.getMainLooper());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO REMOVE
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_login_loginButton:
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        mProgressBarMoreThanAPI20.setVisibility(View.VISIBLE);
                        mProgressBarLessThanAPI21.setVisibility(View.GONE);
                    } else {
                        mProgressBarMoreThanAPI20.setVisibility(View.GONE);
                        mProgressBarLessThanAPI21.setVisibility(View.VISIBLE);
                    }

                    if (EmailEditText.getText().toString().isEmpty()) {
                        EmailEditText.setError(getResources().getString(R.string.required));
                    } else if (CodeEditText.getText().toString().isEmpty()) {
                        CodeEditText.setError(getResources().getString(R.string.required));
                    } else {
                        mPhoneNumber = CodeEditText.getText().toString();
                        mUsername = EmailEditText.getText().toString();

                        mBackgroundHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mUserDatabase.CheckIfUserExists(mPhoneNumber, mUsername, LoginActivity.this);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(ON_CREATE_TAG, "CAMERA Permission is granted");
                //Calling ScannerActivity to Scan QR Code
                Intent intent = new Intent(new Intent(this, ScannerActivity.class));
                intent.putExtra("activity_type", "loginActivity");
                startActivity(intent);
            } else {
                Log.i(ON_CREATE_TAG, "CAMERA Permission is not granted");
            }
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
    public void onLoginSuccess(final boolean state, final String type,final String mUsername) {
        try {
            Log.i(LOGIN_ACTIVITY_TAG, "onLoginSuccess is called");
            if (mChangeUIHandler != null) {
                //check Firebase Database if user or Admin.
                mChangeUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBarMoreThanAPI20.setVisibility(View.GONE);
                        mProgressBarLessThanAPI21.setVisibility(View.GONE);

                        if (state) {
                            addUserIntoOfflineDatabase(type,mUsername);
                            Log.i(LOGIN_ACTIVITY_TAG, "onLoginSuccess state is true and userType is " + type);
                            //update UI and navigate to Home Screen
                            updateUI(type);
                        } else {
                            Log.i(LOGIN_ACTIVITY_TAG, "onLoginSuccess state is false, Entered code not found");
                            Toast.makeText(LoginActivity.this, "اسم المستخدم او كلمة المرور غير صحيح", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAddUserSuccess(boolean state) {

    }

    private void updateUI(String userType) {
        try {
            Log.i(LOGIN_ACTIVITY_TAG, UPDATE_UI_TAG + " is called");
            // if userType is admin
            if (userType.matches(getString(R.string.admin))) {
                //update UI and navigate to Home Screen
                Log.i(LOGIN_ACTIVITY_TAG, UPDATE_UI_TAG + " userType is Admin");
                startActivity(new Intent(this, HomeAdminActivity.class));
                finish();
                //if userType is user
            } else if (userType.matches(getString(R.string.user))) {
                //update UI and navigate to Home Screen
                Log.i(LOGIN_ACTIVITY_TAG, UPDATE_UI_TAG + " userType is User");
                mScan_Text_Result = null;
                startActivity(new Intent(this, HomeUserActivity.class));
                finish();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addUserIntoOfflineDatabase(String type,String mUsername) {
        try {
            SharedPreferences mPreferences = getSharedPreferences(getString(R.string.shared_preferences_file_name), MODE_PRIVATE);
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(getString(R.string.username),mUsername);
            editor.putString(getString(R.string.type_key), type);
            editor.apply();
            Log.i(LOGIN_ACTIVITY_TAG,"Current User is : "+mUsername);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}