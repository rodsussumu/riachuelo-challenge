package com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("Invalid token.");
    }
}
