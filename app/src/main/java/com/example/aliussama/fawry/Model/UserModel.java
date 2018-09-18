package com.example.aliussama.fawry.Model;

import java.io.Serializable;

/**
 * Created by ali Ussama on 7/6/2018.
 */

public class UserModel implements Serializable {

    private String userKey;
    private String id;
    private String name;
    private String email;
    private String type;

    public UserModel(String id, String name, String email, String type) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.type = type;
    }
    public UserModel(String userKey,String id, String name, String email, String type) {
        this.userKey = userKey;
        this.id = id;
        this.name = name;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
