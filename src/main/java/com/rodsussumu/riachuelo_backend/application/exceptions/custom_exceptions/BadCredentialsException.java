package com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions;

public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException() {
        super("Invalid username or password");
    }
}
