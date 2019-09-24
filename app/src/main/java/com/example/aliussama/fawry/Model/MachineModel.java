package com.example.aliussama.fawry.Model;

import java.io.Serializable;

public class MachineModel implements Serializable {

    private String mUID;
    private String mMachineId;
    private String mMachineId2;
    private String mMachineId3;
    private String mMachineId4;
    private String mClientName;
    private String mClientPhone;
    private String mAddress;
    private String mCityName;
    private String mLatitude;
    private String mLongitude;
    private String mRepresentativeName;
    private String userID;

    public MachineModel(String mMachineId,String mMachineId2,String mMachineId3,String mMachineId4, String mClientName, String mClientPhone,String cityName, String mAddress, String mLatitude, String mLongitude,String representativeName,String userId) {
        this.mMachineId = mMachineId;
        this.mMachineId2 = mMachineId2;
        this.mMachineId3 = mMachineId3;
        this.mMachineId4 = mMachineId4;
        this.mClientName = mClientName;
        this.mClientPhone = mClientPhone;
        this.mCityName = cityName;
        this.mAddress = mAddress;
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
        this.mRepresentativeName = representativeName;
        this.userID = userId;
    }

    public MachineModel(String mUID, String mMachineId,String mMachineId2,String mMachineId3,String mMachineId4, String mClientName, String mClientPhone,String cityName, String mAddress, String mLatitude, String mLongitude,String representativeName,String userId) {
        this.mUID = mUID;
        this.mMachineId = mMachineId;
        this.mMachineId2 = mMachineId2;
        this.mMachineId3 = mMachineId3;
        this.mMachineId4 = mMachineId4;
        this.mClientName = mClientName;
        this.mClientPhone = mClientPhone;
        this.mCityName = cityName;
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

    public String getmMachineId2() {
        return mMachineId2;
    }

    public void setmMachineId2(String mMachineId2) {
        this.mMachineId2 = mMachineId2;
    }

    public String getmMachineId3() {
        return mMachineId3;
    }

    public void setmMachineId3(String mMachineId3) {
        this.mMachineId3 = mMachineId3;
    }

    public String getmMachineId4() {
        return mMachineId4;
    }

    public void setmMachineId4(String mMachineId4) {
        this.mMachineId4 = mMachineId4;
    }

    public String getmCityName() {
        return mCityName;
    }

    public void setmCityName(String mCityName) {
        this.mCityName = mCityName;
    }
}
