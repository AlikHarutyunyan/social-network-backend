package com.example.serverProject.Exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(Exception e) {
        super(e);
    }
}
