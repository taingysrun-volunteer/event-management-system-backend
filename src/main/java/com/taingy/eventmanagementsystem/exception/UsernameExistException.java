package com.taingy.eventmanagementsystem.exception;

public class UsernameExistException extends RuntimeException {

    public UsernameExistException(String message) {
        super(message);
    }
}
