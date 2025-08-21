package com.rodsussumu.riachuelo_backend.application.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodsussumu.riachuelo_backend.application.config.TokenService;
import com.rodsussumu.riachuelo_backend.application.dtos.StatusUpdateRequestDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.TaskDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.TaskRequestDTO;
import com.rodsussumu.riachuelo_backend.application.enums.StatusEnum;
import com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.InvalidStatusException;
import com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.OwnershipDeniedException;
import com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.TaskNotFoundException;
import com.rodsussumu.riachuelo_backend.application.services.TaskService;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(TaskControllerTest.TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(reg -> reg.anyRequest().permitAll())
                    .build();
        }
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private TokenService tokenService;

    @Test
    @WithMockUser
    @DisplayName("POST /tasks should return 201 with Location and body")
    void create_shouldReturn201() throws Exception {
        TaskDTO created = TaskDTO.builder()
                .id(1L)
                .description("New")
                .status(StatusEnum.PENDING)
                .build();

        Mockito.when(taskService.create(any(TaskRequestDTO.class))).thenReturn(created);

        TaskRequestDTO req = TaskRequestDTO.builder()
                .description("New")
                .dueDate(new Date())
                .build();

        mvc.perform(post("/tasks")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/tasks/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("New"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /tasks should return 200 with list")
    void list_shouldReturn200() throws Exception {
        Mockito.when(taskService.listAll(isNull(), isNull())).thenReturn(List.of(
                TaskDTO.builder().id(1L).description("A").status(StatusEnum.PENDING).build(),
                TaskDTO.builder().id(2L).description("B").status(StatusEnum.IN_PROGRESS).build()
        ));

        mvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /tasks/{id} should return 200 with body")
    void listById_shouldReturn200() throws Exception {
        Mockito.when(taskService.listById(5L))
                .thenReturn(TaskDTO.builder().id(5L).description("X").status(StatusEnum.DONE).build());

        mvc.perform(get("/tasks/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /tasks/{id} should return 200 with updated body")
    void update_shouldReturn200() throws Exception {
        TaskRequestDTO req = TaskRequestDTO.builder().description("Edited").dueDate(new Date()).build();
        Mockito.when(taskService.updateTask(eq(7L), any(TaskRequestDTO.class)))
                .thenReturn(TaskDTO.builder().id(7L).description("Edited").status(StatusEnum.PENDING).build());

        mvc.perform(put("/tasks/7")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.description").value("Edited"));
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /tasks/{id}/status should return 200 with updated status")
    void updateStatus_shouldReturn200() throws Exception {
        Mockito.when(taskService.updateStatus(8L, "done"))
                .thenReturn(TaskDTO.builder().id(8L).status(StatusEnum.DONE).build());

        StatusUpdateRequestDTO req = new StatusUpdateRequestDTO("done");

        mvc.perform(patch("/tasks/8/status")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /tasks/{id} should return 204")
    void delete_shouldReturn204() throws Exception {
        mvc.perform(delete("/tasks/9"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /tasks/{id} should return 404 when task not found")
    void listById_shouldReturn404_whenNotFound() throws Exception {
        Mockito.when(taskService.listById(55L))
                .thenThrow(new TaskNotFoundException(55L));

        mvc.perform(get("/tasks/55"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not found"))
                .andExpect(jsonPath("$.message").value("Task 55 not found."))
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"));
    }

    // 403 - Ownership negado em GET /tasks/{id}
    @Test
    @WithMockUser
    @DisplayName("GET /tasks/{id} should return 403 when ownership denied")
    void listById_shouldReturn403_whenOwnershipDenied() throws Exception {
        OwnershipDeniedException ex = new OwnershipDeniedException();
        Mockito.when(taskService.listById(66L))
                .thenThrow(ex);

        mvc.perform(get("/tasks/66"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value(ex.getMessage()))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    // 400 - Status inválido em PATCH /tasks/{id}/status
    @Test
    @WithMockUser
    @DisplayName("PATCH /tasks/{id}/status should return 400 when status is invalid")
    void updateStatus_shouldReturn400_whenInvalidStatus() throws Exception {
        Mockito.when(taskService.updateStatus(77L, "DON"))
                .thenThrow(new InvalidStatusException());

        StatusUpdateRequestDTO req = new StatusUpdateRequestDTO("DON");

        mvc.perform(patch("/tasks/77/status")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid status. Use: PENDING, IN_PROGRESS or DONE."))
                .andExpect(jsonPath("$.code").value("TASK_INVALID_STATUS"));
    }

    // 404 - Task não encontrada em DELETE /tasks/{id}
    @Test
    @WithMockUser
    @DisplayName("DELETE /tasks/{id} should return 404 when task not found")
    void delete_shouldReturn404_whenNotFound() throws Exception {
        Mockito.doThrow(new TaskNotFoundException(99L))
                .when(taskService).deleteTask(99L);

        mvc.perform(delete("/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not found"))
                .andExpect(jsonPath("$.message").value("Task 99 not found."))
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"));
    }
}
