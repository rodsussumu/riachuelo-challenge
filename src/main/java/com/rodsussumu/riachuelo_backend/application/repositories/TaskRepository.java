package com.rodsussumu.riachuelo_backend.application.repositories;

import com.rodsussumu.riachuelo_backend.application.enums.StatusEnum;
import com.rodsussumu.riachuelo_backend.application.models.Task;
import com.rodsussumu.riachuelo_backend.application.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser(User user);
    List<Task> findByUserAndStatus(User user, StatusEnum status);
}
