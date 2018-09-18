package com.example.aliussama.fawry.Model;

import java.io.Serializable;

public class MachineModel implements Serializable {

    private String mUID;
    private String mMachineId;
    private String mClientName;
    private String mClientPhone;
    private String mAddress;
    private String mLatitude;
    private String mLongitude;


    public MachineModel(String mMachineId, String mClientName, String mClientPhone, String mAddress, String mLatitude, String mLongitude) {
        this.mMachineId = mMachineId;
        this.mClientName = mClientName;
        this.mClientPhone = mClientPhone;
        this.mAddress = mAddress;
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
    }

    public MachineModel(String mUID, String mMachineId, String mClientName, String mClientPhone, String mAddress, String mLatitude, String mLongitude) {
        this.mUID = mUID;
        this.mMachineId = mMachineId;
        this.mClientName = mClientName;
        this.mClientPhone = mClientPhone;
        this.mAddress = mAddress;
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
    }

    public String getmUID() {
        return mUID;
    }

    public void setmUID(String mUID) {
        this.mUID = mUID;
    }

    public String getmMachineId() {
        return mMachineId;
    }

    public void setmMachineId(String mMachineId) {
        this.mMachineId = mMachineId;
    }

    public String getmClientName() {
        return mClientName;
    }

    public void setmClientName(String mClientName) {
        this.mClientName = mClientName;
    }

    public String getmClientPhone() {
        return mClientPhone;
    }

    public void setmClientPhone(String mClientPhone) {
        this.mClientPhone = mClientPhone;
    }

    public String getmAddress() {
        return mAddress;
    }

    public void setmAddress(String mAddress) {
        this.mAddress = mAddress;
    }

    public String getmLatitude() {
        return mLatitude;
    }

    public void setmLatitude(String mLatitude) {
        this.mLatitude = mLatitude;
    }

    public String getmLongitude() {
        return mLongitude;
    }

    public void setmLongitude(String mLongitude) {
        this.mLongitude = mLongitude;
    }
}
