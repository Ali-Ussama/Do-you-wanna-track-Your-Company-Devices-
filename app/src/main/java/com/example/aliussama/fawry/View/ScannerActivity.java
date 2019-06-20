package com.example.aliussama.fawry.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aliussama.fawry.R;
import com.example.aliussama.fawry.View.User.HomeUserActivity;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    // QR Code Scanner View
    private ZXingScannerView mScannerView;
    private String LOGIN_ACTIVITY_TAG = "loginActivity";
    private String HOME_USER_ACTIVITY = "homeUserActivity";
    private String ALL_MACHINES_REC_ADAPTER = "AllMachinesRecAdapter";

    private String ActivityType = "";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        try {
            Intent intent = getIntent();
            Log.i("ScannerActivity", "onCreate is called");
            ActivityType = intent.getStringExtra("activity_type");

            mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
            setContentView(mScannerView);  // Set the scanner view as the content view
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Log.i("ScannerActivity", "onResume");

            if (mScannerView != null) {
                mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
                mScannerView.startCamera();          // Start camera on resume
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            Log.i("ScannerActivity", "onPause");

            if (mScannerView != null) {
                // Stop camera on pause
                mScannerView.stopCamera();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleResult(final Result result) {
        try {
            if (result != null && !result.getText().isEmpty()) {
                Log.i("handleResult", "Scan Result = " + "Scanner Activity\nresult = " + result.getText());

                Toast.makeText(this, "Scanner Activity\nresult = " + result.getText(), Toast.LENGTH_SHORT).show();
                if (ActivityType.matches(LOGIN_ACTIVITY_TAG)) {
                    if (LoginActivity.mScan_Text_Result != null) {
                    Log.i("handleResult", LOGIN_ACTIVITY_TAG);

                    LoginActivity.mScan_Text_Result = result.getText();
                    } else {
                        Toast.makeText(this, getString(R.string.error_message_to_user), Toast.LENGTH_SHORT).show();
                    }
                } else if (ActivityType.matches(HOME_USER_ACTIVITY)) {
                    HomeUserActivity.mMachineId = result.getText();
                    if (HomeUserActivity.machineCodeEditText != null) {
                    Log.i("handleResult", HOME_USER_ACTIVITY);
                    HomeUserActivity.machineCodeEditText.setText(result.getText());
                    } else {
                        Toast.makeText(this, getString(R.string.error_message_to_user), Toast.LENGTH_SHORT).show();
                    }
                }
//                else if (ActivityType.matches(ALL_MACHINES_REC_ADAPTER)) {
//                    if (AllMachinesRecAdapter.machineCodeEditText != null) {
//                        AllMachinesRecAdapter.machineCodeEditText.setText(result.getText());
//                    } else {
//                        Toast.makeText(this, getString(R.string.error_message_to_user), Toast.LENGTH_SHORT).show();
//                    }
//                }
                onBackPressed();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
