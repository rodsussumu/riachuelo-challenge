package com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions;

public class OwnershipDeniedException extends RuntimeException {
    public OwnershipDeniedException() {
        super("You cannot modify a task you do not own!");
    }
}
