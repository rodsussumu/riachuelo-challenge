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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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

    @MockitoBean
    TaskServiceImpl service;

    @MockitoBean
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
            for (Task t : in) out.add(TaskDTO.builder().id(t.getId()).description(t.getDescription()).status(t.getStatus()).build());
            return out;
        });

        List<TaskDTO> dtos = service.listAll(null, null);

        assertEquals(2, dtos.size());
        assertEquals(1L, dtos.get(0).id());
        assertEquals(2L, dtos.get(1).id());
        verify(taskRepository).findByUser(currentUser);
        verify(taskRepository, never()).findByUserAndStatus(any(), any());
    }

    @Test
    @DisplayName("listAll with status filter calls repository by status")
    void listAll_statusFilter() {
        stubCurrentUser();
        Task c = task(3, StatusEnum.DONE, new Date(), currentUser);
        when(taskRepository.findByUserAndStatus(currentUser, StatusEnum.DONE)).thenReturn(List.of(c));
        when(taskMapper.toDTOList(anyList())).thenReturn(List.of(
                TaskDTO.builder().id(3L).description("T3").status(StatusEnum.DONE).build()
        ));

        List<TaskDTO> dtos = service.listAll(StatusEnum.DONE, null);

        assertEquals(1, dtos.size());
        assertEquals("T3", dtos.get(0).description());
        verify(taskRepository).findByUserAndStatus(currentUser, StatusEnum.DONE);
        verify(taskRepository, never()).findByUser(currentUser);
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
            for (Task t : in) out.add(TaskDTO.builder().id(t.getId()).status(t.getStatus()).description(t.getDescription()).build());
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
            for (Task t : in) out.add(TaskDTO.builder().id(t.getId()).status(t.getStatus()).description(t.getDescription()).build());
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
        when(taskMapper.toDTO(t)).thenReturn(TaskDTO.builder().id(10L).description("T10").status(StatusEnum.IN_PROGRESS).build());

        TaskDTO dto = service.listById(10L);

        assertEquals(10L, dto.id());
        verify(taskRepository).findById(10L);
    }

    @Test
    @DisplayName("listById throws OwnershipDenied when task is from another user")
    void listById_notOwned_throws() {
        stubCurrentUser();
        User other = new User();
        other.setId(99L);
        Task t = task(11, StatusEnum.PENDING, new Date(), other);
        when(taskRepository.findById(11L)).thenReturn(Optional.of(t));

        assertThrows(OwnershipDeniedException.class, () -> service.listById(11L));
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
            return TaskDTO.builder().id(x.getId()).description(x.getDescription()).status(x.getStatus()).build();
        });

        TaskDTO dto = service.updateStatus(20L, "done");

        assertEquals(StatusEnum.DONE, dto.status());
    }

    @Test
    @DisplayName("updateStatus throws InvalidStatus for unknown status")
    void updateStatus_invalid() {
        stubCurrentUser();
        Task t = task(21, StatusEnum.PENDING, new Date(), currentUser);
        when(taskRepository.findById(21L)).thenReturn(Optional.of(t));

        assertThrows(InvalidStatusException.class, () -> service.updateStatus(21L, "weird"));
    }

    @Test
    @DisplayName("create maps request to entity, sets user and returns DTO")
    void create_createsTask() {
        stubCurrentUser();
        TaskRequestDTO req = TaskRequestDTO.builder().description("New").dueDate(new Date()).build();
        Task entity = task(0, StatusEnum.PENDING, req.dueDate(), null);
        when(taskMapper.toEntity(req)).thenReturn(entity);
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task x = inv.getArgument(0);
            x.setId(100L);
            return x;
        });
        when(taskMapper.toDTO(any(Task.class))).thenAnswer(inv -> {
            Task x = inv.getArgument(0);
            return TaskDTO.builder().id(x.getId()).description(x.getDescription()).status(x.getStatus()).build();
        });

        TaskDTO dto = service.create(req);

        assertEquals(100L, dto.id());
        verify(taskRepository).save(argThat(t -> t.getUser() != null && t.getUser().getId().equals(1L)));
    }

    @Test
    @DisplayName("deleteTask removes owned task")
    void delete_deletesOwnedTask() {
        stubCurrentUser();
        Task t = task(30, StatusEnum.DONE, new Date(), currentUser);
        when(taskRepository.findById(30L)).thenReturn(Optional.of(t));

        service.deleteTask(30L);

        verify(taskRepository).delete(t);
    }

    @Test
    @DisplayName("methods throw InvalidToken when user is missing")
    void methods_shouldThrowInvalidTokenWhenUserMissing() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("ghost", "pwd"));
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> service.listAll(null, null));
    }

    @Test
    @DisplayName("listById throws TaskNotFound when id does not exist")
    void listById_taskNotFound() {
        when(taskRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> service.listById(404L));
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    @DisplayName("listAll with unknown sort evaluates else-if as false and keeps order")
    void listAll_unknownSort_keepsOrder() {
        stubCurrentUser();

        Date d1 = new Date(1_000_000L);
        Date d2 = new Date(2_000_000L);
        Task first  = task(1, StatusEnum.PENDING, d1, currentUser);
        Task second = task(2, StatusEnum.PENDING, d2, currentUser);

        when(taskRepository.findByUser(currentUser))
                .thenReturn(new ArrayList<>(List.of(first, second)));

        when(taskMapper.toDTOList(anyList())).thenReturn(List.of());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Task>> captor = ArgumentCaptor.forClass(List.class);

        service.listAll(null, "noop");

        verify(taskMapper).toDTOList(captor.capture());
        List<Task> passed = captor.getValue();
        assertEquals(List.of(1L, 2L), passed.stream().map(Task::getId).toList());
    }

}
