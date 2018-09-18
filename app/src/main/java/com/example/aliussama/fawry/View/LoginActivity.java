package com.example.aliussama.fawry.View;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.aliussama.fawry.R;
import com.example.aliussama.fawry.View.Admin.HomeAdminActivity;
import com.example.aliussama.fawry.Model.Callbacks.UserDatabaseCallback;
import com.example.aliussama.fawry.Model.UserDatabase;
import com.example.aliussama.fawry.View.User.HomeUserActivity;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,
        UserDatabaseCallback {

    final String HANDLER_THREAD_NAME = "LoginActivityThread";
    final String LOGIN_ACTIVITY_TAG = "LoginActivity";
    final String UPDATE_UI_TAG = "updateUI";
    final String ON_CREATE_TAG = "onCreate";

    public static String mScan_Text_Result;

    EditText CodeEditText, EmailEditText;
    Button LoginButton, mScanButton;
    ImageView codeVisibility;
    ProgressBar mProgressBarMoreThanAPI20, mProgressBarLessThanAPI21;
    String CodeValue, EmailValue;

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

            codeVisibility = findViewById(R.id.activity_login_code_visibility);
            codeVisibility.setOnClickListener(this);

            mScanButton = findViewById(R.id.activity_login_scan_qr_code_button);
            mScanButton.setOnClickListener(this);

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

    @Override
    protected void onResume() {
        try {
            Log.i(LOGIN_ACTIVITY_TAG, "onResume");

            if (mScan_Text_Result != null && !mScan_Text_Result.isEmpty()) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    mProgressBarMoreThanAPI20.setVisibility(View.VISIBLE);
                    mProgressBarLessThanAPI21.setVisibility(View.GONE);
                } else {
                    mProgressBarMoreThanAPI20.setVisibility(View.GONE);
                    mProgressBarLessThanAPI21.setVisibility(View.VISIBLE);
                }
                Log.i(LOGIN_ACTIVITY_TAG, "onResume: Scan Text Result = " + mScan_Text_Result);

                String result[] = mScan_Text_Result.split(",");

                Log.i(LOGIN_ACTIVITY_TAG, "onResume: username = " + result[0]);
                Log.i(LOGIN_ACTIVITY_TAG, "onResume: email = " + result[1]);
                String userCode = mScan_Text_Result;
                String userEmail = result[1];
                if (mUserDatabase == null) {
                    mUserDatabase = new UserDatabase();
                }
                mUserDatabase.CheckIfUserExists(userCode, userEmail, this);

                mScan_Text_Result = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                        CodeValue = CodeEditText.getText().toString();
                        EmailValue = EmailEditText.getText().toString();

                        if (!Patterns.EMAIL_ADDRESS.matcher(EmailValue).matches()) {
                            EmailEditText.setError(getResources().getString(R.string.email_error));
                        } else {
                            mBackgroundHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mUserDatabase.CheckIfUserExists(CodeValue, EmailValue, LoginActivity.this);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.activity_login_code_visibility:
                try {
                    if (CodeEditText.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                        CodeEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        codeVisibility.setImageResource(R.drawable.ic_baseline_visibility_24px);
                    } else {
                        CodeEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        codeVisibility.setImageResource(R.drawable.ic_baseline_visibility_off_24px);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.activity_login_scan_qr_code_button:
                try {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED) {

                        Log.i(ON_CREATE_TAG, "CAMERA Permission is granted");

                        //Calling ScannerActivity to Scan QR Code
                        Intent intent = new Intent(new Intent(this, ScannerActivity.class));
                        intent.putExtra("activity_type", "loginActivity");
                        startActivity(intent);
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
    public void onLoginSuccess(final boolean state, final String type) {
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
                            addUserIntoOfflineDatabase(type);
                            Log.i(LOGIN_ACTIVITY_TAG, "onLoginSuccess state is true and userType is " + type);
                            //update UI and navigate to Home Screen
                            updateUI(type);
                        } else {
                            Log.i(LOGIN_ACTIVITY_TAG, "onLoginSuccess state is false, Entered code not found");
                            Toast.makeText(LoginActivity.this, "البريد الالكتروني/الرقم السري غير صحيح", Toast.LENGTH_SHORT).show();
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

    private void addUserIntoOfflineDatabase(String type) {
        try {
            SharedPreferences mPreferences = getSharedPreferences(getString(R.string.shared_preferences_file_name), MODE_PRIVATE);
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(getString(R.string.type_key), type);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}