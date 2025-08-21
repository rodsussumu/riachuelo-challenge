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
import com.rodsussumu.riachuelo_backend.application.services.TaskService;
import jakarta.persistence.EntityNotFoundException;
import org.apache.coyote.BadRequestException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public TaskServiceImpl(
        TaskRepository taskRepository,
        UserRepository userRepository,
        TaskMapper taskMapper
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(InvalidTokenException::new);
    }

    private Task getOwnedTaskOrThrow(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        User current = getAuthenticatedUser();
        if (task.getUser() == null || !task.getUser().getId().equals(current.getId())) {
            throw new OwnershipDeniedException();
        }
        return task;
    }

    public TaskDTO create(TaskRequestDTO dto) {
        User user = getAuthenticatedUser();
        Task task = taskMapper.toEntity(dto);
        task.setUser(user);
        return taskMapper.toDTO(taskRepository.save(task));
    }

    public List<TaskDTO> listAll() {
        User user = getAuthenticatedUser();
        return taskMapper.toDTOList(taskRepository.findByUser(user));
    }

    public TaskDTO listById(Long id) {
        Task task = getOwnedTaskOrThrow(id);
        return taskMapper.toDTO(task);
    }

    public TaskDTO updateTask(Long id, TaskRequestDTO dto) {
        Task task = getOwnedTaskOrThrow(id);
        taskMapper.updateFromRequest(dto, task);
        return taskMapper.toDTO(taskRepository.save(task));
    }

    public TaskDTO updateStatus(Long id, String status) throws BadRequestException {
        Task task = getOwnedTaskOrThrow(id);
        try {
            StatusEnum statusName = StatusEnum.valueOf(status.toUpperCase());
            task.setStatus(statusName);
            return taskMapper.toDTO(taskRepository.save(task));
        } catch (IllegalArgumentException ex) {
            throw new InvalidStatusException();
        }
    }

    public void deleteTask(Long id) {
        Task task = getOwnedTaskOrThrow(id);
        taskRepository.delete(task);
    }
}
