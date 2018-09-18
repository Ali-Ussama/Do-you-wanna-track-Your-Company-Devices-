package com.example.aliussama.fawry.View.Admin;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
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

public class HomeAdminActivity extends AppCompatActivity implements UserDatabaseCallback,
        SearchView.OnQueryTextListener {

    final String HANDLER_THREAD_NAME = "HomeAdminActivityThread";
    final String HOME_ADMIN_TAG = "HomeAdminActivity";
    Toolbar toolbar;
    SearchView searchView;
    SearchManager searchManager;
    EditText searchEditText;
    ProgressBar mProgressBarMoreThanAPI20;
    EditText usernameEditText, emailEditText;
    ImageView qrCodeImageView;
    UserDatabase mUserDatabase;
    UserModel user;
    HandlerThread mThread;
    Handler mBackgroundHandler;
    Handler mChangeUIHandler;
    String userCode = "";

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
            emailEditText = findViewById(R.id.activity_home_admin_user_email_edit_text);
            qrCodeImageView = findViewById(R.id.activity_home_admin_generated_code_image_view);
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

    private void deleteLocalSharedPreferences(String string) {
        try {
            SharedPreferences mPreferences = getSharedPreferences(string, MODE_PRIVATE);
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(getString(R.string.type_key), getString(R.string.default_value_of_shared_preferences_string));
            editor.apply();
            Log.i(HOME_ADMIN_TAG, "deleteLocalSharedPreferences(): type is None");

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
            case R.id.activity_home_admin_generate_code_fab_button:
                // Whatever you need to encode in the QR code
                if (!usernameEditText.getText().toString().isEmpty() && !emailEditText.getText().toString().isEmpty()) {
                    userCode = (usernameEditText.getText().toString().concat(",").concat(emailEditText.getText().toString())).toLowerCase();

                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                    try {
                        BitMatrix bitMatrix = multiFormatWriter.encode(userCode, BarcodeFormat.QR_CODE, 200, 200);
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                        qrCodeImageView.setImageBitmap(bitmap);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }
                if (usernameEditText.getText().toString().isEmpty()) {
                    usernameEditText.setError(getResources().getString(R.string.enter_username));
                }
                if (emailEditText.getText().toString().isEmpty()) {
                    emailEditText.setError(getResources().getString(R.string.enter_user_email));
                }
                break;
            case R.id.activity_home_admin_add_user_button:
                try {

                    mProgressBarMoreThanAPI20.setVisibility(View.VISIBLE);

                    if (usernameEditText.getText().toString().isEmpty()) {
                        usernameEditText.setError(getResources().getString(R.string.enter_username));
                    } else if (emailEditText.getText().toString().isEmpty()) {
                        emailEditText.setError(getResources().getString(R.string.enter_user_email));
                    } else {
                        final String code = (usernameEditText.getText().toString().concat(",").concat(emailEditText.getText().toString())).toLowerCase();

                        if (userCode != null && !userCode.isEmpty() && code.matches(userCode)) {
                            user = new UserModel(code, usernameEditText.getText().toString(), emailEditText.getText().toString(), "user");
                            final String email = emailEditText.getText().toString().toLowerCase();

                            mBackgroundHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mUserDatabase.CheckIfUserExists(code, email, HomeAdminActivity.this);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else if (userCode == null) {
                            Toast.makeText(this, "من فضلك أنشئ الكود الخاس بالمستخدم", Toast.LENGTH_SHORT).show();
                        } else if (!code.matches(userCode)) {
                            Toast.makeText(this, "الكود لا يتماثل مع البيانات المدخله، من فضلك انشئ الكود", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                    qrCodeImageView.setImageResource(R.color.grey_light);
                    usernameEditText.setText("");
                    emailEditText.setText("");
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
}
