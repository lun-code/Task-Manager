package com.example.task_manager_api.service;

import com.example.task_manager_api.dto.task.TaskCreateDTO;
import com.example.task_manager_api.dto.task.TaskPageResponseDTO;
import com.example.task_manager_api.dto.task.TaskPatchDTO;
import com.example.task_manager_api.dto.task.TaskResponseDTO;
import com.example.task_manager_api.entity.Category;
import com.example.task_manager_api.exception.DataConflictException;
import com.example.task_manager_api.exception.ResourceNotFoundException;
import com.example.task_manager_api.repository.CategoryRepository;
import com.example.task_manager_api.repository.TaskRepository;
import com.example.task_manager_api.specification.TaskSpecification;
import lombok.RequiredArgsConstructor;
import com.example.task_manager_api.entity.Task;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    private final CategoryRepository categoryRepository;

    public TaskPageResponseDTO findAll(Boolean completed, Long categoryId, int page, int size) {
        // 1. Construimos el filtro combinando las Specifications
        // Si ambos parámetros son null, no se aplica ningún filtro y devuelve todo
        Specification<Task> filters = Specification
                .where(TaskSpecification.hasCompleted(completed))
                .and(TaskSpecification.hasCategory(categoryId));

        // 2. Construimos el objeto de paginación: qué página y cuantos elementos por página
        Pageable pageable = PageRequest.of(page, size);

        // 3. Consultamos la BD con los filtros y la paginación aplicados
        Page<Task> taskPage = taskRepository.findAll(filters, pageable);

        // 4. Construimos el DTO de respuesta con los datos de la página
        return new TaskPageResponseDTO(
                taskPage.getContent().stream().map(this::buildResponse).toList(),
                taskPage.getNumber(),
                taskPage.getTotalPages(),
                taskPage.getTotalElements()
        );
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

        // Este try-catch no debería ejecutarse nunca, ya que ninguna tabla apunta a Task con FK.
        // Se mantiene por seguridad defensiva, por si en el futuro se añaden relaciones nuevas.
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