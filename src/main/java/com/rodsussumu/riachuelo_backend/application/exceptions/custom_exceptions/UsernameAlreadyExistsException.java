package com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions;

public class UsernameAlreadyExistsException extends RuntimeException {
  public UsernameAlreadyExistsException() {
    super("Username already registered.");
  }
}
