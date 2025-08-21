package com.rodsussumu.riachuelo_backend.application.exceptions;

import com.rodsussumu.riachuelo_backend.application.dtos.ErrorResponseDTO;
import com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponseDTO> build(
            HttpStatus status,
            String error,
            String message,
            String code
    ) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                status.value(),
                error,
                message,
                code
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(InvalidStatusException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidStatus(InvalidStatusException ex) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(),"TASK_INVALID_STATUS");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentialsSpring(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), "BAD_CREDENTIALS");
    }

    @ExceptionHandler(com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentialsCustom(
            com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), "BAD_CREDENTIALS");
    }

    @ExceptionHandler(OwnershipDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleOwnershipDenied(OwnershipDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "Unauthorized", ex.getMessage(), "FORBIDDEN");
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleTaskNotFound(TaskNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Not found", ex.getMessage(), "TASK_NOT_FOUND");
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidToken(InvalidTokenException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), "INVALID_TOKEN");
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), "USERNAME_ALREADY_EXISTS");
    }
}