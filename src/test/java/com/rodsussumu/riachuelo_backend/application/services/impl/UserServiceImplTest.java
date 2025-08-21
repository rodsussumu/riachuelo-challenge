package com.rodsussumu.riachuelo_backend.application.services.impl;

import com.rodsussumu.riachuelo_backend.application.config.TokenService;
import com.rodsussumu.riachuelo_backend.application.dtos.UserAuthDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.UserAuthResponseDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.UserRegisterResponseDTO;
import com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.BadCredentialsException;
import com.rodsussumu.riachuelo_backend.application.models.User;
import com.rodsussumu.riachuelo_backend.application.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;
    private TokenService tokenService;
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl service;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        authenticationManager = mock(AuthenticationManager.class);
        tokenService = mock(TokenService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new UserServiceImpl(userRepository, authenticationManager, tokenService, passwordEncoder);
    }

    @Test
    @DisplayName("register should create user when username not taken")
    void register_shouldCreateUser() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123")).thenReturn("ENC_123");

        UserRegisterResponseDTO resp = service.register(new UserAuthDTO("john", "123"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("john", userCaptor.getValue().getUsername());
        assertEquals("ENC_123", userCaptor.getValue().getPassword());
        assertEquals("john", resp.username());
        assertEquals("User created", resp.message());
    }

    @Test
    @DisplayName("register should throw when username already exists")
    void register_shouldThrowOnDuplicate() {
        User existing = new User();
        existing.setId(1L);
        existing.setUsername("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(existing));
        assertThrows(RuntimeException.class, () -> service.register(new UserAuthDTO("john", "123")));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login should authenticate and return token")
    void login_shouldAuthenticateAndReturnToken() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("john");
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(new User()));
        when(tokenService.generateToken("john")).thenReturn("tkn");

        UserAuthResponseDTO resp = service.login(new UserAuthDTO("john", "123"));

        assertEquals("john", resp.username());
        assertEquals("tkn", resp.token());
        verify(authenticationManager).authenticate(any());
        verify(tokenService).generateToken("john");
    }

    @Test
    @DisplayName("login should throw BadCredentialsException when repository returns null Optional reference")
    void login_shouldThrowWhenRepoReturnsNullReference() {
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByUsername("john")).thenReturn(null);

        assertThrows(BadCredentialsException.class, () -> service.login(new UserAuthDTO("john", "123")));

        verify(authenticationManager).authenticate(any());
        verify(tokenService, never()).generateToken(any());
    }

}
