package com.example.task_manager_api.controller;

import com.example.task_manager_api.dto.task.TaskCreateDTO;
import com.example.task_manager_api.dto.task.TaskPatchDTO;
import com.example.task_manager_api.dto.task.TaskResponseDTO;
import com.example.task_manager_api.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks() {
        List<TaskResponseDTO> tasks = taskService.findAll();

        return ResponseEntity.ok(tasks); // HTTP 200 OK y DTO
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getTaskById(@PathVariable UUID id) {
        TaskResponseDTO task = taskService.findById(id);

        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskCreateDTO taskCreateDTO) {
        TaskResponseDTO savedTask = taskService.save(taskCreateDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask); // HTTP 201 Created y DTO
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> patchTask(@PathVariable UUID id, @RequestBody TaskPatchDTO taskPatchDTO) {
        TaskResponseDTO patchedTask = taskService.patch(id, taskPatchDTO);

        return ResponseEntity.ok(patchedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        taskService.delete(id);

        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}