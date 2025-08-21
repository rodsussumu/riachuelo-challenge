package com.rodsussumu.riachuelo_backend.application.services;

import com.rodsussumu.riachuelo_backend.application.dtos.TaskDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.TaskRequestDTO;
import com.rodsussumu.riachuelo_backend.application.enums.StatusEnum;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface TaskService {
    TaskDTO create(TaskRequestDTO dto);
    List<TaskDTO> listAll();
    TaskDTO updateTask(Long id, TaskRequestDTO dto);
    TaskDTO updateStatus(Long id, StatusEnum status) throws BadRequestException;
    void deleteTask(Long id);
}
