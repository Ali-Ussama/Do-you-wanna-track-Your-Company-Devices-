package com.example.aliussama.fawry.Model.Callbacks;

import com.example.aliussama.fawry.Model.MachineModel;
import com.example.aliussama.fawry.Model.UserModel;

public interface SearchActivityCallback {

    void onUserItemDelete(UserModel user);

    void onUserItemUpdate(UserModel user);

    void onMachineItemDelete(MachineModel machine);

    void onMachineItemUpdate(MachineModel machine);

    void onMachineDeletedSuccess(boolean status);

    void onMachineDeleteFailure(Exception e);

    void onMachineUpdatedSuccess(boolean status);

    void onMachineUpdatedFailure(Exception e);

}
