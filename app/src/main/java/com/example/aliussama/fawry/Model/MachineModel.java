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
    private String mRepresentativeName;
    private String userID;

    public MachineModel(String mMachineId, String mClientName, String mClientPhone, String mAddress, String mLatitude, String mLongitude,String representativeName,String userId) {
        this.mMachineId = mMachineId;
        this.mClientName = mClientName;
        this.mClientPhone = mClientPhone;
        this.mAddress = mAddress;
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
        this.mRepresentativeName = representativeName;
        this.userID = userId;
    }

    public MachineModel(String mUID, String mMachineId, String mClientName, String mClientPhone, String mAddress, String mLatitude, String mLongitude,String representativeName,String userId) {
        this.mUID = mUID;
        this.mMachineId = mMachineId;
        this.mClientName = mClientName;
        this.mClientPhone = mClientPhone;
        this.mAddress = mAddress;
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
        this.mRepresentativeName = representativeName;
        this.userID = userId;
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

    public String getmRepresentativeName() {
        return mRepresentativeName;
    }

    public void setmRepresentativeName(String mRepresentativeName) {
        this.mRepresentativeName = mRepresentativeName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
