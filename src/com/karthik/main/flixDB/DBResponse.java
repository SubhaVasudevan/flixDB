package com.karthik.main.flixDB;

import java.io.Serializable;

public class DBResponse implements Serializable{
    String responseStatus;
    DBItem[] items;

    public DBResponse(String responseStatus, DBItem[] items) {
        this.responseStatus = responseStatus;
        this.items = items;
    }

    public DBResponse(String responseStatus) {
        this(responseStatus, null);
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseStatus() {
        return this.responseStatus;
    }

    public void setItems(DBItem[] items) {
        this.items = items;
    }

    public DBItem[] getItems() {
        return this.items;
    }
}
