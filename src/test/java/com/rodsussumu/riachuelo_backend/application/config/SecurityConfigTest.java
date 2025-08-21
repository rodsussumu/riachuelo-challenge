package com.rodsussumu.riachuelo_backend.application.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodsussumu.riachuelo_backend.application.dtos.UserAuthDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.UserAuthResponseDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.UserRegisterResponseDTO;
import com.rodsussumu.riachuelo_backend.application.services.TaskService;
import com.rodsussumu.riachuelo_backend.application.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
class SecurityConfigTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockitoBean
    TokenService tokenService;

    @MockitoBean
    UserService userService;

    @MockitoBean
    TaskService taskService;

    @Test
    @DisplayName("POST /auth/login is permitAll and returns body")
    void login_permitAll() throws Exception {
        when(userService.login(any(UserAuthDTO.class)))
                .thenReturn(UserAuthResponseDTO.builder()
                        .username("john")
                        .token("tkn")
                        .build());

        mvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserAuthDTO("john", "123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.token").value("tkn"));
    }

    @Test
    @DisplayName("POST /auth/register is permitAll and returns 201")
    void register_permitAll() throws Exception {
        when(userService.register(any(UserAuthDTO.class)))
                .thenReturn(UserRegisterResponseDTO.builder()
                        .username("john")
                        .message("User created")
                        .build());

        mvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UserAuthDTO("john", "123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.message").value("User created"));
    }

    @Test
    @DisplayName("GET /tasks requires authentication -> 401 without user")
    void tasks_requiresAuthentication_unauthorized() throws Exception {
        mvc.perform(get("/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /tasks returns 200 with authenticated user")
    void tasks_allowsAuthenticated() throws Exception {
        when(taskService.listAll(isNull(), isNull())).thenReturn(List.of());
        mvc.perform(get("/tasks"))
                .andExpect(status().isOk());
    }
}
