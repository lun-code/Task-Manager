package com.example.task_manager_api.service;

import com.example.task_manager_api.dto.task.TaskCreateDTO;
import com.example.task_manager_api.dto.task.TaskPageResponseDTO;
import com.example.task_manager_api.dto.task.TaskPatchDTO;
import com.example.task_manager_api.dto.task.TaskResponseDTO;
import com.example.task_manager_api.entity.Category;
import com.example.task_manager_api.entity.User;
import com.example.task_manager_api.exception.ResourceNotFoundException;
import com.example.task_manager_api.repository.CategoryRepository;
import com.example.task_manager_api.repository.TaskRepository;
import com.example.task_manager_api.specification.TaskSpecification;
import lombok.RequiredArgsConstructor;
import com.example.task_manager_api.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

    // Método auxiliar privado — evita repetir el cast en cada método
    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public TaskPageResponseDTO findAll(Boolean completed, Long categoryId, int page, int size) {
        User currentUser = getCurrentUser();

        Specification<Task> filters = Specification
                .where(TaskSpecification.hasCompleted(completed))
                .and(TaskSpecification.hasCategory(categoryId))
                .and(TaskSpecification.hasUser(currentUser));

        Pageable pageable = PageRequest.of(page, size);
        Page<Task> taskPage = taskRepository.findAll(filters, pageable);

        return new TaskPageResponseDTO(
                taskPage.getContent().stream().map(this::buildResponse).toList(),
                taskPage.getNumber(),
                taskPage.getTotalPages(),
                taskPage.getTotalElements()
        );
    }

    public TaskResponseDTO findById(UUID id) {
        User currentUser = getCurrentUser();

        // findByIdAndUser en vez de findById — ya filtra por usuario
        Task task = taskRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        return buildResponse(task);
    }

    public TaskResponseDTO patch(UUID id, TaskPatchDTO taskPatchDTO) {
        User currentUser = getCurrentUser();

        Task task = taskRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));

        if (taskPatchDTO.title() != null)       task.setTitle(taskPatchDTO.title());
        if (taskPatchDTO.description() != null) task.setDescription(taskPatchDTO.description());
        if (taskPatchDTO.completed() != null)   task.setCompleted(taskPatchDTO.completed());

        if (taskPatchDTO.categoryID() != null) {
            Category category = categoryRepository.findById(taskPatchDTO.categoryID())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + taskPatchDTO.categoryID()));
            task.setCategory(category);
        }

        return buildResponse(taskRepository.save(task));
    }

    public void delete(UUID id) {
        User currentUser = getCurrentUser();

        // Si no existe o no es del usuario → mismo 404 (no revelas que existe pero es ajena)
        taskRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        taskRepository.deleteById(id);
    }

    public TaskResponseDTO save(TaskCreateDTO taskCreateDTO) {
        User currentUser = getCurrentUser();

        Category category = categoryRepository.findById(taskCreateDTO.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + taskCreateDTO.categoryId()));

        Task task = Task.builder()
                .title(taskCreateDTO.title())
                .description(taskCreateDTO.description())
                .category(category)
                .user(currentUser)
                .build();

        return buildResponse(taskRepository.save(task));
    }

    private TaskResponseDTO buildResponse(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .completed(task.isCompleted())
                .createdAt(task.getCreatedAt())
                .categoryName(task.getCategory() != null ? task.getCategory().getName() : "Sin categoría")
                .build();
    }
}