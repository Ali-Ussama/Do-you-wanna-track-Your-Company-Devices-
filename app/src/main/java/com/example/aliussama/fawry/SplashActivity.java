package com.example.aliussama.fawry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aliussama.fawry.View.Admin.HomeAdminActivity;
import com.example.aliussama.fawry.View.LoginActivity;
import com.example.aliussama.fawry.View.User.HomeUserActivity;

public class SplashActivity extends AppCompatActivity {
    private final String TAG = "SplashActivity";
    final int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkIfLoggedInBefore();
            }
        }, SPLASH_TIME_OUT);
    }

    private void checkIfLoggedInBefore() {
        try {
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_preferences_file_name), MODE_PRIVATE);
            String type = sharedPref.getString(getString(R.string.type_key), getString(R.string.default_value_of_shared_preferences_string));

            Log.i(TAG, "checkIfLoggedInBefore(): type is " + type);

            if (!type.matches(getString(R.string.default_value_of_shared_preferences_string))) {
                updateUI(type);
            } else {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUI(String userType) {
        try {
            Log.i(TAG, "updateUI: is called");
            // if userType is admin
            if (userType.matches(getString(R.string.admin))) {
                //update UI and navigate to Home Screen
                Log.i(TAG, "updateUI: userType is Admin");
                startActivity(new Intent(this, HomeAdminActivity.class));
                finish();
                //if userType is user
            } else if (userType.matches(getString(R.string.user))) {
                //update UI and navigate to Home Screen
                Log.i(TAG, "updateUI: userType is User");

                startActivity(new Intent(this, HomeUserActivity.class));
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
