package com.example.task_manager_api.controller;

import com.example.task_manager_api.config.TestSecurityConfig;
import com.example.task_manager_api.dto.task.TaskCreateDTO;
import com.example.task_manager_api.dto.task.TaskPageResponseDTO;
import com.example.task_manager_api.dto.task.TaskResponseDTO;
import com.example.task_manager_api.exception.ResourceNotFoundException;
import com.example.task_manager_api.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(TestSecurityConfig.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    // ========================
    // GET /api/tasks
    // ========================

    @Test
    @WithMockUser
    void getAllTasks_shouldReturn200WithPage() throws Exception {
        // GIVEN
        TaskResponseDTO task = TaskResponseDTO.builder()
                .id(UUID.randomUUID())
                .title("Tarea 1")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .categoryName("Estudio")
                .build();

        TaskPageResponseDTO page = new TaskPageResponseDTO(List.of(task), 0, 1, 1L);
        when(taskService.findAll(null, null, 0, 10)).thenReturn(page);

        // WHEN + THEN
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[0].title").value("Tarea 1"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser
    void getAllTasks_shouldReturn200WithFilters() throws Exception {
        // GIVEN
        TaskPageResponseDTO emptyPage = new TaskPageResponseDTO(List.of(), 0, 0, 0L);
        when(taskService.findAll(true, 1L, 0, 10)).thenReturn(emptyPage);

        // WHEN + THEN
        mockMvc.perform(get("/api/tasks?completed=true&categoryId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ========================
    // GET /api/tasks/{id}
    // ========================

    @Test
    @WithMockUser
    void getTaskById_shouldReturn200_whenExists() throws Exception {
        // GIVEN
        UUID id = UUID.randomUUID();
        TaskResponseDTO task = TaskResponseDTO.builder()
                .id(id)
                .title("Tarea 1")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .categoryName("Estudio")
                .build();
        when(taskService.findById(id)).thenReturn(task);

        // WHEN + THEN
        mockMvc.perform(get("/api/tasks/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tarea 1"));
    }

    @Test
    @WithMockUser
    void getTaskById_shouldReturn404_whenNotExists() throws Exception {
        // GIVEN
        UUID id = UUID.randomUUID();
        when(taskService.findById(id)).thenThrow(new ResourceNotFoundException("Task not found"));

        // WHEN + THEN
        mockMvc.perform(get("/api/tasks/" + id))
                .andExpect(status().isNotFound());
    }

    // ========================
    // POST /api/tasks
    // ========================

    @Test
    @WithMockUser
    void createTask_shouldReturn201_whenValid() throws Exception {
        // GIVEN
        TaskCreateDTO dto = new TaskCreateDTO("Tarea 1", "Descripción", 1L);
        TaskResponseDTO response = TaskResponseDTO.builder()
                .id(UUID.randomUUID())
                .title("Tarea 1")
                .description("Descripción")
                .completed(false)
                .createdAt(LocalDateTime.now())
                .categoryName("Estudio")
                .build();
        when(taskService.save(any(TaskCreateDTO.class))).thenReturn(response);

        // WHEN + THEN
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Tarea 1"));
    }

    @Test
    @WithMockUser
    void createTask_shouldReturn400_whenTitleIsBlank() throws Exception {
        // GIVEN
        TaskCreateDTO dto = new TaskCreateDTO("", "Descripción", 1L);

        // WHEN + THEN
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void createTask_shouldReturn400_whenCategoryIdIsNull() throws Exception {
        // GIVEN
        TaskCreateDTO dto = new TaskCreateDTO("Tarea 1", "Descripción", null);

        // WHEN + THEN
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ========================
    // PATCH /api/tasks/{id}
    // ========================

    @Test
    @WithMockUser
    void patchTask_shouldReturn200_whenValid() throws Exception {
        // GIVEN
        UUID id = UUID.randomUUID();
        TaskResponseDTO response = TaskResponseDTO.builder()
                .id(id)
                .title("Tarea actualizada")
                .completed(true)
                .createdAt(LocalDateTime.now())
                .categoryName("Estudio")
                .build();
        when(taskService.patch(eq(id), any())).thenReturn(response);

        // WHEN + THEN
        mockMvc.perform(patch("/api/tasks/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Tarea actualizada\", \"completed\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tarea actualizada"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    @WithMockUser
    void patchTask_shouldReturn404_whenNotExists() throws Exception {
        // GIVEN
        UUID id = UUID.randomUUID();
        when(taskService.patch(eq(id), any()))
                .thenThrow(new ResourceNotFoundException("Task not found with id " + id));

        // WHEN + THEN
        mockMvc.perform(patch("/api/tasks/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Tarea actualizada\"}"))
                .andExpect(status().isNotFound());
    }

    // ========================
    // DELETE /api/tasks/{id}
    // ========================

    @Test
    @WithMockUser
    void deleteTask_shouldReturn204_whenExists() throws Exception {
        // GIVEN
        UUID id = UUID.randomUUID();
        doNothing().when(taskService).delete(id);

        // WHEN + THEN
        mockMvc.perform(delete("/api/tasks/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteTask_shouldReturn404_whenNotExists() throws Exception {
        // GIVEN
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Task not found with id " + id))
                .when(taskService).delete(id);

        // WHEN + THEN
        mockMvc.perform(delete("/api/tasks/" + id))
                .andExpect(status().isNotFound());
    }
}