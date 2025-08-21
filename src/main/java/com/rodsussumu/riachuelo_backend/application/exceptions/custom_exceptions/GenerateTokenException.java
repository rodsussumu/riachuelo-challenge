package com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions;

public class GenerateTokenException extends RuntimeException {
    public GenerateTokenException() {
      super("Error Generating Token");
    }
}
