package com.example.aliussama.fawry.Model;

import java.io.Serializable;

/**
 * Created by ali Ussama on 7/6/2018.
 */

public class UserModel implements Serializable {

    private String userKey;
    private String id;
    private String name;
    private String phone;
    private String type;

    public UserModel(String id, String name, String phone, String type) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.type = type;
    }
    public UserModel(String userKey, String id, String name, String phone, String type) {
        this.userKey = userKey;
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.type = type;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
