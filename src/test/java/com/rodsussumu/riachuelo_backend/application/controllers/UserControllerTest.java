package com.rodsussumu.riachuelo_backend.application.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodsussumu.riachuelo_backend.application.config.TokenService;
import com.rodsussumu.riachuelo_backend.application.dtos.UserAuthDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.UserAuthResponseDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.UserRegisterResponseDTO;
import com.rodsussumu.riachuelo_backend.application.exceptions.GlobalExceptionHandler;
import com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.BadCredentialsException;
import com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.UsernameAlreadyExistsException;
import com.rodsussumu.riachuelo_backend.application.services.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TokenService tokenService;

    @Test
    @DisplayName("POST /auth/register should return 201 with body")
    void register_shouldReturn201() throws Exception {
        UserRegisterResponseDTO out = UserRegisterResponseDTO.builder()
                .username("john")
                .message("User created")
                .build();

        Mockito.when(userService.register(new UserAuthDTO("john", "123")))
                .thenReturn(out);

        mvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserAuthDTO("john", "123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.message").value("User created"));
    }

    @Test
    @DisplayName("POST /auth/login should return 200 with authenticated=true")
    void login_shouldReturn200() throws Exception {
        UserAuthResponseDTO out = UserAuthResponseDTO.builder()
                .username("john")
                .authenticated(true)
                .build();

        Mockito.when(userService.login(new UserAuthDTO("john", "123")))
                .thenReturn(out);

        mvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserAuthDTO("john", "123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.authenticated").value(true));
    }

    @Test
    @DisplayName("POST /auth/login should return 401 when bad credentials")
    void login_shouldReturn401_whenBadCredentials() throws Exception {
        Mockito.when(userService.login(new UserAuthDTO("john", "wrong")))
                .thenThrow(new BadCredentialsException());

        mvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserAuthDTO("john", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.code").value("BAD_CREDENTIALS"));
    }

    @Test
    @DisplayName("POST /auth/register should return 409 when username already exists")
    void register_shouldReturn409_whenUsernameTaken() throws Exception {
        Mockito.when(userService.register(new UserAuthDTO("john", "123")))
                .thenThrow(new UsernameAlreadyExistsException());

        mvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserAuthDTO("john", "123"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Username already registered."))
                .andExpect(jsonPath("$.code").value("USERNAME_ALREADY_EXISTS"));
    }
}
