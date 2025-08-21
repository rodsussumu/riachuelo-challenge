package com.rodsussumu.riachuelo_backend.application.services.impl;

import com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.InvalidTokenException;
import com.rodsussumu.riachuelo_backend.application.models.User;
import com.rodsussumu.riachuelo_backend.application.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl service;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setPassword("12345");
    }

    @Test
    @DisplayName("should load user when username exists")
    void shouldLoadUserWhenExists() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        UserDetails result = service.loadUserByUsername("john");
        assertNotNull(result);
        assertEquals("john", result.getUsername());
        assertEquals("12345", result.getPassword());
    }

    @Test
    @DisplayName("should throw InvalidTokenException when username not found")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(InvalidTokenException.class, () -> service.loadUserByUsername("unknown"));
    }
}
