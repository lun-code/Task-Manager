package com.example.task_manager_api.service;

import com.example.task_manager_api.dto.task.TaskCreateDTO;
import com.example.task_manager_api.dto.task.TaskPatchDTO;
import com.example.task_manager_api.dto.task.TaskResponseDTO;
import com.example.task_manager_api.entity.Category;
import com.example.task_manager_api.exception.DataConflictException;
import com.example.task_manager_api.exception.ResourceNotFoundException;
import com.example.task_manager_api.repository.CategoryRepository;
import com.example.task_manager_api.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import com.example.task_manager_api.entity.Task;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    private final CategoryRepository categoryRepository;

    public List<TaskResponseDTO> findAll() {
        return taskRepository.findAll()
                .stream() // Convertimos la lista en un flujo de datos
                .map(this::buildResponse) // Pasamos cada Task por el traductor
                .toList(); // Volvemos a agrupar en una lista
    }

    public TaskResponseDTO findById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        return buildResponse(task);
    }

    public TaskResponseDTO save(TaskCreateDTO taskCreateDTO) {

        Category category = categoryRepository.findById(taskCreateDTO.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + taskCreateDTO.categoryId()));

        // Pasar DTO -> Entity usando el Builder de la entidad
        Task task = Task.builder()
                .title(taskCreateDTO.title())
                .description(taskCreateDTO.description())
                .category(category)
                .build();

        Task savedTask = taskRepository.save(task);

        return buildResponse(savedTask);
    }

    public TaskResponseDTO patch(UUID id, TaskPatchDTO taskPatchDTO) {
        // 1. Buscar la tarea original
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));

        // 2. Actualizar solo los campos que no sean nulos
        if (taskPatchDTO.title() != null) {
            task.setTitle(taskPatchDTO.title());
        }

        if (taskPatchDTO.description() != null) {
            task.setDescription(taskPatchDTO.description());
        }

        if (taskPatchDTO.completed() != null) {
            task.setCompleted(taskPatchDTO.completed());
        }

        // 3. Lógica para actualizar la categoría en el Patch
        if (taskPatchDTO.categoryID() != null) {
            Category category = categoryRepository.findById(taskPatchDTO.categoryID())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + taskPatchDTO.categoryID()));
            task.setCategory(category);
        }

        // 4. Guardar cambios
        Task patchedTask = taskRepository.save(task);

        return buildResponse(patchedTask);
    }

    public void delete(UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }

        try {
            taskRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DataConflictException("You can't delete this task because it is already in use");
        }
    }

    private TaskResponseDTO buildResponse(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .completed(task.isCompleted())
                .createdAt(task.getCreatedAt())
                .categoryName(task.getCategory() != null ? task.getCategory().getName() : "Sin categoría") // Realmente no es necesario comprobar NULL, porque se protege en la entidad.
                .build();
    }
}