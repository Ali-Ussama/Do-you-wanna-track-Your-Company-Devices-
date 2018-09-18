package com.example.aliussama.fawry.Model.Callbacks;

public interface UserDatabaseCallback {

    void onLoginSuccess(boolean state, String Type);

    void onAddUserSuccess(boolean state);
}
