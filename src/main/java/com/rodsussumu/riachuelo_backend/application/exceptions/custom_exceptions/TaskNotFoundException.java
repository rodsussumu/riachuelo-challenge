package com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions;

public class TaskNotFoundException extends RuntimeException {
  public TaskNotFoundException(Long id) {
    super("Task " + id + " not found.");
  }
}
