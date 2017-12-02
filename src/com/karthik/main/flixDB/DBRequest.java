package com.karthik.main.flixDB;

import java.io.Serializable;

public class DBRequest implements Serializable {
    String requestType;
    DBItem item;

    public DBRequest(String requestType, DBItem item) {
        this.requestType = requestType;
        this.item = item;
    }

    public DBRequest(String requestType) {
        this(requestType, null);
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestType() {
        return this.requestType;
    }

    public void setItem(DBItem item) {
        this.item = item;
    }

    public DBItem getItem() {
        return this.item;
    }
}
