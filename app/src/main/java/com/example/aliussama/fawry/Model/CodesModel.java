package com.example.aliussama.fawry.Model;

import java.io.Serializable;

public class CodesModel implements Serializable {

    String id;
    String code;

    public CodesModel(String id, String code) {
        this.id = id;
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
