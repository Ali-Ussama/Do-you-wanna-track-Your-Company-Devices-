package com.example.aliussama.fawry.Model.Callbacks;

public interface OnAddMachineListener {

    void onMachineExists(boolean status);

    void onAddMachineSuccess(boolean status);

    void onAddMachineFailure(Exception e);
}
