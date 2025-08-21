package com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions;

public class InvalidStatusException extends RuntimeException {
    public InvalidStatusException() {
        super("Invalid status. Use: PENDING, IN_PROGRESS or DONE.");
    }
}
