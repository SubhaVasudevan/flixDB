package com.karthik.main.flixDB;

import java.io.Serializable;

public class DBItem implements Serializable{
    private String key;
    private String value;

    public DBItem(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public DBItem(String key) {
        this(key, null);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
