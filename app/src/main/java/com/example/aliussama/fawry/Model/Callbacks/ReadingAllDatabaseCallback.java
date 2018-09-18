package com.example.aliussama.fawry.Model.Callbacks;

import com.example.aliussama.fawry.Model.MachineModel;
import com.example.aliussama.fawry.Model.UserModel;

import java.util.ArrayList;

public interface ReadingAllDatabaseCallback {
    void onAllUsersSuccess(ArrayList<UserModel> users);
    void onAllUsersFailure(Exception e);
    void onAllMachinesSuccess(ArrayList<MachineModel> machines);
    void onAllMachinesFailure(String message);

}
