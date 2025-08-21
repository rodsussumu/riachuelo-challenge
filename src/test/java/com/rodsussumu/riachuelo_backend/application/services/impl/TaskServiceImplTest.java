package com.rodsussumu.riachuelo_backend.application.services.impl;

import com.rodsussumu.riachuelo_backend.application.dtos.TaskDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.TaskRequestDTO;
import com.rodsussumu.riachuelo_backend.application.enums.StatusEnum;
import com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.InvalidStatusException;
import com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.InvalidTokenException;
import com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.OwnershipDeniedException;
import com.rodsussumu.riachuelo_backend.application.exceptions.custom_exceptions.TaskNotFoundException;
import com.rodsussumu.riachuelo_backend.application.mappers.TaskMapper;
import com.rodsussumu.riachuelo_backend.application.models.Task;
import com.rodsussumu.riachuelo_backend.application.models.User;
import com.rodsussumu.riachuelo_backend.application.repositories.TaskRepository;
import com.rodsussumu.riachuelo_backend.application.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TaskServiceImplTest {

    private TaskRepository taskRepository;
    private UserRepository userRepository;
    private TaskMapper taskMapper;
    private TaskServiceImpl service;
    private final User currentUser = new User();

    @BeforeEach
    void setup() {
        taskRepository = mock(TaskRepository.class);
        userRepository = mock(UserRepository.class);
        taskMapper = mock(TaskMapper.class);
        service = new TaskServiceImpl(taskRepository, userRepository, taskMapper);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("john");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        currentUser.setId(1L);
        currentUser.setUsername("john");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(currentUser));
    }

    @Test
    @DisplayName("create should persist task with authenticated user and return DTO")
    void create_shouldPersistWithUser() {
        TaskRequestDTO request = TaskRequestDTO.builder()
                .description("New task")
                .dueDate(new Date())
                .build();

        Task entity = new Task();
        entity.setDescription("New task");

        Task saved = new Task();
        saved.setId(10L);
        saved.setDescription("New task");
        saved.setUser(currentUser);

        TaskDTO dto = TaskDTO.builder().id(10L).description("New task").status(StatusEnum.PENDING).build();

        when(taskMapper.toEntity(request)).thenReturn(entity);
        when(taskRepository.save(entity)).thenReturn(saved);
        when(taskMapper.toDTO(saved)).thenReturn(dto);

        TaskDTO result = service.create(request);

        assertEquals(10L, result.id());
        verify(taskRepository).save(entity);
        assertEquals(currentUser, saved.getUser());
    }

    @Test
    @DisplayName("listAll should return only current user's tasks")
    void listAll_shouldReturnUserTasks() {
        Task t1 = new Task(); t1.setId(1L); t1.setUser(currentUser);
        Task t2 = new Task(); t2.setId(2L); t2.setUser(currentUser);
        List<Task> tasks = List.of(t1, t2);

        TaskDTO d1 = TaskDTO.builder().id(1L).build();
        TaskDTO d2 = TaskDTO.builder().id(2L).build();
        List<TaskDTO> dtos = List.of(d1, d2);

        when(taskRepository.findByUser(currentUser)).thenReturn(tasks);
        when(taskMapper.toDTOList(tasks)).thenReturn(dtos);

        List<TaskDTO> result = service.listAll();
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals(2L, result.get(1).id());
    }

    @Test
    @DisplayName("listById should return DTO when task is owned by user")
    void listById_shouldReturnWhenOwned() {
        Task task = new Task();
        task.setId(5L); task.setUser(currentUser);

        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));
        when(taskMapper.toDTO(task)).thenReturn(TaskDTO.builder().id(5L).build());

        TaskDTO dto = service.listById(5L);
        assertEquals(5L, dto.id());
    }

    @Test
    @DisplayName("listById should throw TaskNotFoundException when id does not exist")
    void listById_shouldThrowWhenNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(TaskNotFoundException.class, () -> service.listById(99L));
    }

    @Test
    @DisplayName("listById should throw OwnershipDeniedException when task belongs to another user")
    void listById_shouldThrowWhenNotOwned() {
        User other = new User(); other.setId(77L);
        Task task = new Task(); task.setId(6L); task.setUser(other);
        when(taskRepository.findById(6L)).thenReturn(Optional.of(task));
        assertThrows(OwnershipDeniedException.class, () -> service.listById(6L));
    }

    @Test
    @DisplayName("updateTask should apply mapper.updateFromRequest and save")
    void updateTask_shouldUseMapperAndSave() {
        TaskRequestDTO request = TaskRequestDTO.builder().description("edited").dueDate(new Date()).build();
        Task existing = new Task(); existing.setId(7L); existing.setUser(currentUser);
        Task saved = new Task(); saved.setId(7L); saved.setUser(currentUser); saved.setDescription("edited");
        TaskDTO dto = TaskDTO.builder().id(7L).description("edited").build();

        when(taskRepository.findById(7L)).thenReturn(Optional.of(existing));
        doAnswer(inv -> {
            TaskRequestDTO r = inv.getArgument(0);
            Task t = inv.getArgument(1);
            t.setDescription(r.description());
            return null;
        }).when(taskMapper).updateFromRequest(eq(request), any(Task.class));
        when(taskRepository.save(existing)).thenReturn(saved);
        when(taskMapper.toDTO(saved)).thenReturn(dto);

        TaskDTO result = service.updateTask(7L, request);
        assertEquals("edited", result.description());
        verify(taskRepository).save(existing);
    }

    @Test
    @DisplayName("updateStatus should accept case-insensitive string and persist")
    void updateStatus_shouldAcceptStringAndPersist() throws Exception {
        Task existing = new Task(); existing.setId(8L); existing.setUser(currentUser);
        when(taskRepository.findById(8L)).thenReturn(Optional.of(existing));

        Task saved = new Task(); saved.setId(8L); saved.setUser(currentUser); saved.setStatus(StatusEnum.DONE);
        when(taskRepository.save(existing)).thenReturn(saved);

        TaskDTO dto = TaskDTO.builder().id(8L).status(StatusEnum.DONE).build();
        when(taskMapper.toDTO(saved)).thenReturn(dto);

        TaskDTO result = service.updateStatus(8L, "done");
        assertEquals(StatusEnum.DONE, result.status());
        verify(taskRepository).save(existing);
    }

    @Test
    @DisplayName("updateStatus should throw InvalidStatusException for invalid values")
    void updateStatus_shouldThrowOnInvalid() {
        Task existing = new Task(); existing.setId(11L); existing.setUser(currentUser);
        when(taskRepository.findById(11L)).thenReturn(Optional.of(existing));
        assertThrows(InvalidStatusException.class, () -> service.updateStatus(11L, "DON"));
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteTask should delete when task is owned by user")
    void deleteTask_shouldDelete() {
        Task existing = new Task(); existing.setId(9L); existing.setUser(currentUser);
        when(taskRepository.findById(9L)).thenReturn(Optional.of(existing));
        service.deleteTask(9L);
        verify(taskRepository).delete(existing);
    }

    @Test
    @DisplayName("methods depending on authenticated user should throw InvalidTokenException when user cannot be resolved")
    void methods_shouldThrowInvalidTokenWhenUserMissing() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        assertThrows(InvalidTokenException.class, () -> service.listAll());
    }

    @Test
    @DisplayName("listById should throw OwnershipDeniedException when task has no user")
    void listById_shouldThrowWhenTaskHasNoUser() {
        Task task = new Task();
        task.setId(12L);
        task.setUser(null);

        when(taskRepository.findById(12L)).thenReturn(Optional.of(task));

        assertThrows(OwnershipDeniedException.class, () -> service.listById(12L));
    }

}
