package com.example.aliussama.fawry.Model.Callbacks;

public interface UserDatabaseCallback {

    void onLoginSuccess(boolean state, String Type,String username);

    void onAddUserSuccess(boolean state);
}
