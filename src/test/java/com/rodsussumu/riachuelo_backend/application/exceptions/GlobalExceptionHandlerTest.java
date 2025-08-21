package com.rodsussumu.riachuelo_backend.application.exceptions;

import com.rodsussumu.riachuelo_backend.application.dtos.ErrorResponseDTO;
import com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("handleInvalidStatus -> 400")
    void handleInvalidStatus_400() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        ResponseEntity<ErrorResponseDTO> resp = h.handleInvalidStatus(new InvalidStatusException());
        assertEquals(400, resp.getStatusCode().value());
        assertEquals(400, resp.getBody().status());
        assertEquals("Bad Request", resp.getBody().error());
        assertEquals("Invalid status. Use: PENDING, IN_PROGRESS or DONE.", resp.getBody().message());
        assertEquals("TASK_INVALID_STATUS", resp.getBody().code());
    }

    @Test
    @DisplayName("handleBadCredentials (Spring) -> 401")
    void handleBadCredentialsSpring_401() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        ResponseEntity<ErrorResponseDTO> resp = h.handleBadCredentialsSpring(
                new BadCredentialsException("Invalid username or password")
        );
        assertEquals(401, resp.getStatusCode().value());
        assertEquals(401, resp.getBody().status());
        assertEquals("Unauthorized", resp.getBody().error());
        assertEquals("Invalid username or password", resp.getBody().message());
        assertEquals("BAD_CREDENTIALS", resp.getBody().code());
    }

    @Test
    @DisplayName("handleBadCredentials (custom) -> 401")
    void handleBadCredentialsCustom_401() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        ResponseEntity<ErrorResponseDTO> resp = h.handleBadCredentialsCustom(
                new com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.BadCredentialsException()
        );
        assertEquals(401, resp.getStatusCode().value());
        assertEquals(401, resp.getBody().status());
        assertEquals("Unauthorized", resp.getBody().error());
        assertEquals("Invalid username or password", resp.getBody().message());
        assertEquals("BAD_CREDENTIALS", resp.getBody().code());
    }

    @Test
    @DisplayName("handleOwnershipDenied -> 403")
    void handleOwnershipDenied_403() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        ResponseEntity<ErrorResponseDTO> resp = h.handleOwnershipDenied(new OwnershipDeniedException());
        assertEquals(403, resp.getStatusCode().value());
        assertEquals(403, resp.getBody().status());
        assertEquals("Unauthorized", resp.getBody().error());
        assertEquals(new OwnershipDeniedException().getMessage(), resp.getBody().message());
        assertEquals("FORBIDDEN", resp.getBody().code());
    }

    @Test
    @DisplayName("handleTaskNotFound -> 404")
    void handleTaskNotFound_404() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        ResponseEntity<ErrorResponseDTO> resp = h.handleTaskNotFound(new TaskNotFoundException(7L));
        assertEquals(404, resp.getStatusCode().value());
        assertEquals(404, resp.getBody().status());
        assertEquals("Not found", resp.getBody().error());
        assertEquals("Task 7 not found.", resp.getBody().message());
        assertEquals("TASK_NOT_FOUND", resp.getBody().code());
    }

    @Test
    @DisplayName("handleInvalidToken -> 401")
    void handleInvalidToken_401() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        ResponseEntity<ErrorResponseDTO> resp = h.handleInvalidToken(new InvalidTokenException());
        assertEquals(401, resp.getStatusCode().value());
        assertEquals(401, resp.getBody().status());
        assertEquals("Unauthorized", resp.getBody().error());
        assertEquals("Invalid token.", resp.getBody().message());
        assertEquals("INVALID_TOKEN", resp.getBody().code());
    }

    @Test
    @DisplayName("handleUsernameAlreadyExists -> 409")
    void handleUsernameAlreadyExists_409() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        var resp = h.handleUsernameAlreadyExists(new UsernameAlreadyExistsException());
        assertEquals(409, resp.getStatusCode().value());
        assertEquals(409, resp.getBody().status());
        assertEquals("Conflict", resp.getBody().error());
        assertEquals("Username already registered.", resp.getBody().message());
        assertEquals("USERNAME_ALREADY_EXISTS", resp.getBody().code());
    }
}