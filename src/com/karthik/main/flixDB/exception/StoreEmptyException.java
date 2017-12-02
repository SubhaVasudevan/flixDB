package com.karthik.main.flixDB.exception;

public class StoreEmptyException extends Exception {
    public StoreEmptyException()
    {
        super();
    }
    public StoreEmptyException(final String message)
    {
        super(message);
    }
}
