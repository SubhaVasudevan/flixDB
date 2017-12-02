package com.karthik.main.flixDB.exception;

public class ItemNotFoundException extends Exception {
    public ItemNotFoundException() {
        super();
    }

    public ItemNotFoundException(final String message) {
        super(message);
    }
}
