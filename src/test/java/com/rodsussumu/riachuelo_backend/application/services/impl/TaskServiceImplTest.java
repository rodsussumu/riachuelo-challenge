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
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    TaskRepository taskRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    TaskMapper taskMapper;

    TaskServiceImpl service;
    User currentUser;

    @BeforeEach
    void setUp() {
        service = new TaskServiceImpl(taskRepository, userRepository, taskMapper);
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("alice");
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("alice", "pwd"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void stubCurrentUser() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(currentUser));
    }

    private Task task(long id, StatusEnum status, Date due, User user) {
        Task t = new Task();
        t.setId(id);
        t.setTitle("Task " + id);   // ✅ sempre setar title
        t.setDescription("T" + id);
        t.setStatus(status);
        t.setDueDate(due);
        t.setUser(user);
        return t;
    }

    @Test
    @DisplayName("listAll(null, null) returns user tasks")
    void listAll_shouldReturnUserTasks() {
        stubCurrentUser();
        Task a = task(1, StatusEnum.PENDING, new Date(System.currentTimeMillis() + 86_400_000), currentUser);
        Task b = task(2, StatusEnum.DONE, new Date(System.currentTimeMillis() + 172_800_000), currentUser);
        when(taskRepository.findByUser(currentUser)).thenReturn(List.of(a, b));
        when(taskMapper.toDTOList(anyList())).thenAnswer(inv -> {
            List<Task> in = inv.getArgument(0);
            List<TaskDTO> out = new ArrayList<>();
            for (Task t : in) out.add(TaskDTO.builder()
                    .id(t.getId())
                    .title(t.getTitle())
                    .description(t.getDescription())
                    .status(t.getStatus())
                    .build());
            return out;
        });

        List<TaskDTO> dtos = service.listAll(null, null);

        assertEquals(2, dtos.size());
        assertEquals("Task 1", dtos.get(0).title());
        assertEquals("Task 2", dtos.get(1).title());
        verify(taskRepository).findByUser(currentUser);
    }

    @Test
    @DisplayName("listAll with status filter calls repository by status")
    void listAll_statusFilter() {
        stubCurrentUser();
        Task c = task(3, StatusEnum.DONE, new Date(), currentUser);
        when(taskRepository.findByUserAndStatus(currentUser, StatusEnum.DONE)).thenReturn(List.of(c));
        when(taskMapper.toDTOList(anyList())).thenReturn(List.of(
                TaskDTO.builder().id(3L).title("Task 3").description("T3").status(StatusEnum.DONE).build()
        ));

        List<TaskDTO> dtos = service.listAll(StatusEnum.DONE, null);

        assertEquals(1, dtos.size());
        assertEquals("Task 3", dtos.get(0).title());
    }

    @Test
    @DisplayName("listAll sorts by dueDate ascending")
    void listAll_sortDueDateAsc() {
        stubCurrentUser();
        Date later = new Date(System.currentTimeMillis() + 172_800_000);
        Date sooner = new Date(System.currentTimeMillis() + 86_400_000);
        Task t1 = task(1, StatusEnum.PENDING, later, currentUser);
        Task t2 = task(2, StatusEnum.PENDING, sooner, currentUser);
        when(taskRepository.findByUser(currentUser)).thenReturn(new ArrayList<>(List.of(t1, t2)));
        when(taskMapper.toDTOList(anyList())).thenAnswer(inv -> {
            List<Task> in = inv.getArgument(0);
            List<TaskDTO> out = new ArrayList<>();
            for (Task t : in) out.add(TaskDTO.builder()
                    .id(t.getId())
                    .title(t.getTitle())
                    .description(t.getDescription())
                    .status(t.getStatus())
                    .build());
            return out;
        });

        List<TaskDTO> dtos = service.listAll(null, "dueDateAsc");

        assertEquals(List.of(2L, 1L), dtos.stream().map(TaskDTO::id).toList());
    }

    @Test
    @DisplayName("listAll sorts by dueDate descending")
    void listAll_sortDueDateDesc() {
        stubCurrentUser();
        Date later = new Date(System.currentTimeMillis() + 172_800_000);
        Date sooner = new Date(System.currentTimeMillis() + 86_400_000);
        Task t1 = task(1, StatusEnum.PENDING, later, currentUser);
        Task t2 = task(2, StatusEnum.PENDING, sooner, currentUser);
        when(taskRepository.findByUser(currentUser)).thenReturn(new ArrayList<>(List.of(t1, t2)));
        when(taskMapper.toDTOList(anyList())).thenAnswer(inv -> {
            List<Task> in = inv.getArgument(0);
            List<TaskDTO> out = new ArrayList<>();
            for (Task t : in) out.add(TaskDTO.builder()
                    .id(t.getId())
                    .title(t.getTitle())
                    .description(t.getDescription())
                    .status(t.getStatus())
                    .build());
            return out;
        });

        List<TaskDTO> dtos = service.listAll(null, "dueDateDesc");

        assertEquals(List.of(1L, 2L), dtos.stream().map(TaskDTO::id).toList());
    }

    @Test
    @DisplayName("listById returns DTO when task is owned")
    void listById_owned() {
        stubCurrentUser();
        Task t = task(10, StatusEnum.IN_PROGRESS, new Date(), currentUser);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(t));
        when(taskMapper.toDTO(t)).thenReturn(TaskDTO.builder()
                .id(10L)
                .title("Task 10")
                .description("T10")
                .status(StatusEnum.IN_PROGRESS)
                .build());

        TaskDTO dto = service.listById(10L);

        assertEquals("Task 10", dto.title());
    }

    @Test
    @DisplayName("updateStatus updates when valid")
    void updateStatus_valid() throws BadRequestException {
        stubCurrentUser();
        Task t = task(20, StatusEnum.PENDING, new Date(), currentUser);
        when(taskRepository.findById(20L)).thenReturn(Optional.of(t));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toDTO(any(Task.class))).thenAnswer(inv -> {
            Task x = inv.getArgument(0);
            return TaskDTO.builder().id(x.getId()).title(x.getTitle()).description(x.getDescription()).status(x.getStatus()).build();
        });

        TaskDTO dto = service.updateStatus(20L, "done");

        assertEquals(StatusEnum.DONE, dto.status());
        assertEquals("Task 20", dto.title());
    }

    @Test
    @DisplayName("create maps request to entity, sets user and returns DTO")
    void create_createsTask() {
        stubCurrentUser();
        TaskRequestDTO req = TaskRequestDTO.builder()
                .title("My Task")
                .description("New")
                .dueDate(new Date())
                .build();

        Task entity = task(0, StatusEnum.PENDING, req.dueDate(), null);
        entity.setTitle(req.title());

        when(taskMapper.toEntity(req)).thenReturn(entity);
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task x = inv.getArgument(0);
            x.setId(100L);
            return x;
        });
        when(taskMapper.toDTO(any(Task.class))).thenAnswer(inv -> {
            Task x = inv.getArgument(0);
            return TaskDTO.builder()
                    .id(x.getId())
                    .title(x.getTitle())
                    .description(x.getDescription())
                    .status(x.getStatus())
                    .build();
        });

        TaskDTO dto = service.create(req);

        assertEquals("My Task", dto.title());
        assertEquals(100L, dto.id());
    }

}
