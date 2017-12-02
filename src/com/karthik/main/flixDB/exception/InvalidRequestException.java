package com.karthik.main.flixDB.exception;

public class InvalidRequestException extends Exception {
    public InvalidRequestException() {
        super();
    }

    public InvalidRequestException(final String message) {
        super(message);
    }
}
