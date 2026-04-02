package com.example.task_manager_api.service;

import com.example.task_manager_api.dto.task.TaskCreateDTO;
import com.example.task_manager_api.dto.task.TaskPageResponseDTO;
import com.example.task_manager_api.dto.task.TaskPatchDTO;
import com.example.task_manager_api.dto.task.TaskResponseDTO;
import com.example.task_manager_api.entity.Category;
import com.example.task_manager_api.entity.Task;
import com.example.task_manager_api.entity.User;
import com.example.task_manager_api.exception.ResourceNotFoundException;
import com.example.task_manager_api.repository.CategoryRepository;
import com.example.task_manager_api.repository.TaskRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TaskService taskService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ========================
    // findAll
    // ========================

    @Test
    void findAll_shouldReturnPageWithTasks() {
        // GIVEN
        Category category = Category.builder().id(1L).name("Estudio").build();
        Task task = Task.builder()
                .id(UUID.randomUUID())
                .title("Tarea 1")
                .completed(false)
                .category(category)
                .user(mockUser)
                .build();

        Page<Task> page = new PageImpl<>(List.of(task), PageRequest.of(0, 10), 1);
        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), any(PageRequest.class))).thenReturn(page);

        // WHEN
        TaskPageResponseDTO result = taskService.findAll(null, null, 0, 10);

        // THEN
        assertThat(result.tasks()).hasSize(1);
        assertThat(result.tasks().get(0).title()).isEqualTo("Tarea 1");
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.currentPage()).isEqualTo(0);
    }

    @Test
    void findAll_shouldReturnEmptyPage_whenNoTasks() {
        // GIVEN
        Page<Task> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(taskRepository.findAll(ArgumentMatchers.<Specification<Task>>any(), any(PageRequest.class))).thenReturn(emptyPage);

        // WHEN
        TaskPageResponseDTO result = taskService.findAll(null, null, 0, 10);

        // THEN
        assertThat(result.tasks()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(0);
    }

    // ========================
    // findById
    // ========================

    @Test
    void findById_shouldReturnTask_whenExists() {
        // GIVEN
        UUID id = UUID.randomUUID();
        Category category = Category.builder().id(1L).name("Estudio").build();
        Task task = Task.builder()
                .id(id)
                .title("Tarea 1")
                .completed(false)
                .category(category)
                .user(mockUser)
                .build();
        when(taskRepository.findByIdAndUser(id, mockUser)).thenReturn(Optional.of(task));

        // WHEN
        TaskResponseDTO result = taskService.findById(id);

        // THEN
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.title()).isEqualTo("Tarea 1");
    }

    @Test
    void findById_shouldThrowException_whenNotExists() {
        // GIVEN
        UUID id = UUID.randomUUID();
        when(taskRepository.findByIdAndUser(id, mockUser)).thenReturn(Optional.empty());

        // THEN
        assertThatThrownBy(() -> taskService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========================
    // save
    // ========================

    @Test
    void save_shouldReturnSavedTask() {
        // GIVEN
        Category category = Category.builder().id(1L).name("Estudio").build();
        TaskCreateDTO dto = new TaskCreateDTO("Tarea 1", "Descripción", 1L);
        Task saved = Task.builder()
                .id(UUID.randomUUID())
                .title("Tarea 1")
                .description("Descripción")
                .completed(false)
                .category(category)
                .user(mockUser)
                .build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(taskRepository.save(any(Task.class))).thenReturn(saved);

        // WHEN
        TaskResponseDTO result = taskService.save(dto);

        // THEN
        assertThat(result.title()).isEqualTo("Tarea 1");
        assertThat(result.categoryName()).isEqualTo("Estudio");
    }

    @Test
    void save_shouldThrowException_whenCategoryNotExists() {
        // GIVEN
        TaskCreateDTO dto = new TaskCreateDTO("Tarea 1", "Descripción", 99L);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // THEN
        assertThatThrownBy(() -> taskService.save(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ========================
    // patch
    // ========================

    @Test
    void patch_shouldReturnUpdatedTask() {
        // GIVEN
        UUID id = UUID.randomUUID();
        Category category = Category.builder().id(1L).name("Estudio").build();
        Task task = Task.builder()
                .id(id)
                .title("Tarea 1")
                .completed(false)
                .category(category)
                .user(mockUser)
                .build();
        TaskPatchDTO dto = new TaskPatchDTO("Tarea actualizada", null, true, null);
        Task updated = Task.builder()
                .id(id)
                .title("Tarea actualizada")
                .completed(true)
                .category(category)
                .user(mockUser)
                .build();
        when(taskRepository.findByIdAndUser(id, mockUser)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(updated);

        // WHEN
        TaskResponseDTO result = taskService.patch(id, dto);

        // THEN
        assertThat(result.title()).isEqualTo("Tarea actualizada");
        assertThat(result.completed()).isTrue();
    }

    @Test
    void patch_shouldThrowException_whenTaskNotExists() {
        // GIVEN
        UUID id = UUID.randomUUID();
        TaskPatchDTO dto = new TaskPatchDTO("Tarea actualizada", null, null, null);
        when(taskRepository.findByIdAndUser(id, mockUser)).thenReturn(Optional.empty());

        // THEN
        assertThatThrownBy(() -> taskService.patch(id, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void patch_shouldThrowException_whenCategoryNotExists() {
        // GIVEN
        UUID id = UUID.randomUUID();
        Category category = Category.builder().id(1L).name("Estudio").build();
        Task task = Task.builder()
                .id(id)
                .title("Tarea 1")
                .completed(false)
                .category(category)
                .user(mockUser)
                .build();
        TaskPatchDTO dto = new TaskPatchDTO(null, null, null, 99L);
        when(taskRepository.findByIdAndUser(id, mockUser)).thenReturn(Optional.of(task));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // THEN
        assertThatThrownBy(() -> taskService.patch(id, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ========================
    // delete
    // ========================

    @Test
    void delete_shouldDeleteTask_whenExists() {
        // GIVEN
        UUID id = UUID.randomUUID();
        Category category = Category.builder().id(1L).name("Estudio").build();
        Task task = Task.builder()
                .id(id)
                .title("Tarea 1")
                .completed(false)
                .category(category)
                .user(mockUser)
                .build();
        when(taskRepository.findByIdAndUser(id, mockUser)).thenReturn(Optional.of(task));

        // WHEN
        taskService.delete(id);

        // THEN
        verify(taskRepository, times(1)).deleteById(id);
    }

    @Test
    void delete_shouldThrowException_whenNotExists() {
        // GIVEN
        UUID id = UUID.randomUUID();
        when(taskRepository.findByIdAndUser(id, mockUser)).thenReturn(Optional.empty());

        // THEN
        assertThatThrownBy(() -> taskService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}