package com.example.aliussama.fawry;

import android.app.Application;

import java.util.Locale;

/**
 * Created by ali Ussama on 7/14/2018.
 */

public class appConfig extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Locale.setDefault(new Locale("ar"));


    }
}
