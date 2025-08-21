package com.rodsussumu.riachuelo_backend.application.controllers;
import com.rodsussumu.riachuelo_backend.application.dtos.StatusUpdateRequest;
import com.rodsussumu.riachuelo_backend.application.dtos.TaskDTO;
import com.rodsussumu.riachuelo_backend.application.dtos.TaskRequestDTO;
import com.rodsussumu.riachuelo_backend.application.services.TaskService;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskDTO> create(@RequestBody TaskRequestDTO dto) {
        TaskDTO created = taskService.create(dto);
        return ResponseEntity
                .created(URI.create("/tasks/" + created.id()))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<TaskDTO>> list() {
        return ResponseEntity.ok(taskService.listAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> update(@PathVariable Long id,
                                          @RequestBody TaskRequestDTO dto) {
        return ResponseEntity.ok(taskService.updateTask(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskDTO> updateStatus(@PathVariable Long id,
                                                @RequestBody StatusUpdateRequest statusUpdateRequest) throws BadRequestException {
        return ResponseEntity.ok(taskService.updateStatus(id, statusUpdateRequest.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
