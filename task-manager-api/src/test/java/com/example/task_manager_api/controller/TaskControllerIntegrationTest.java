package com.example.task_manager_api.controller;

import com.example.task_manager_api.BaseIntegrationTest;
import com.example.task_manager_api.dto.user.LoginUserDto;
import com.example.task_manager_api.dto.user.RegisterUserDto;
import com.example.task_manager_api.repository.CategoryRepository;
import com.example.task_manager_api.repository.TaskRepository;
import com.example.task_manager_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private String token;
    private Long categoryId;

    @BeforeEach
    void setUp() throws Exception {
        taskRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Registramos y logueamos para obtener el token
        RegisterUserDto registerDto = new RegisterUserDto("test@test.com", "password123", "Test User");
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)));

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginUserDto("test@test.com", "password123"))))
                .andReturn().getResponse().getContentAsString();

        token = objectMapper.readTree(loginResponse).get("token").asText();

        // Creamos una categoría para usarla en los tests de tareas
        String categoryResponse = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Estudio\"}"))
                .andReturn().getResponse().getContentAsString();

        categoryId = objectMapper.readTree(categoryResponse).get("id").asLong();
    }

    // ========================
    // GET /api/tasks
    // ========================

    @Test
    void getAllTasks_shouldReturn200WithEmptyPage() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getAllTasks_shouldReturn200WithTasks() throws Exception {
        // Creamos una tarea
        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Tarea 1\", \"categoryId\": " + categoryId + "}"));

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks[0].title").value("Tarea 1"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAllTasks_shouldFilterByCompleted() throws Exception {
        // Creamos dos tareas
        String taskResponse = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Tarea 1\", \"categoryId\": " + categoryId + "}"))
                .andReturn().getResponse().getContentAsString();

        String taskId = objectMapper.readTree(taskResponse).get("id").asText();

        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Tarea 2\", \"categoryId\": " + categoryId + "}"));

        // Marcamos la primera como completada
        mockMvc.perform(patch("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"completed\": true}"));

        // Filtramos por completadas
        mockMvc.perform(get("/api/tasks?completed=true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.tasks[0].title").value("Tarea 1"));
    }

    @Test
    void getAllTasks_shouldReturn403_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isForbidden());
    }

    // ========================
    // GET /api/tasks/{id}
    // ========================

    @Test
    void getTaskById_shouldReturn200_whenExists() throws Exception {
        String taskResponse = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Tarea 1\", \"categoryId\": " + categoryId + "}"))
                .andReturn().getResponse().getContentAsString();

        String taskId = objectMapper.readTree(taskResponse).get("id").asText();

        mockMvc.perform(get("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tarea 1"))
                .andExpect(jsonPath("$.categoryName").value("Estudio"));
    }

    @Test
    void getTaskById_shouldReturn404_whenNotExists() throws Exception {
        mockMvc.perform(get("/api/tasks/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ========================
    // POST /api/tasks
    // ========================

    @Test
    void createTask_shouldReturn201AndPersist() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Tarea 1\", \"categoryId\": " + categoryId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Tarea 1"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.categoryName").value("Estudio"));

        assertThat(taskRepository.findAll()).hasSize(1);
    }

    @Test
    void createTask_shouldReturn400_whenTitleIsBlank() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"\", \"categoryId\": " + categoryId + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_shouldReturn404_whenCategoryNotExists() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Tarea 1\", \"categoryId\": 9999}"))
                .andExpect(status().isNotFound());
    }

    // ========================
    // PATCH /api/tasks/{id}
    // ========================

    @Test
    void patchTask_shouldReturn200AndUpdate() throws Exception {
        String taskResponse = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Tarea 1\", \"categoryId\": " + categoryId + "}"))
                .andReturn().getResponse().getContentAsString();

        String taskId = objectMapper.readTree(taskResponse).get("id").asText();

        mockMvc.perform(patch("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Tarea actualizada\", \"completed\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tarea actualizada"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void patchTask_shouldReturn404_whenNotExists() throws Exception {
        mockMvc.perform(patch("/api/tasks/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Tarea actualizada\"}"))
                .andExpect(status().isNotFound());
    }

    // ========================
    // DELETE /api/tasks/{id}
    // ========================

    @Test
    void deleteTask_shouldReturn204AndRemoveFromDatabase() throws Exception {
        String taskResponse = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Tarea 1\", \"categoryId\": " + categoryId + "}"))
                .andReturn().getResponse().getContentAsString();

        String taskId = objectMapper.readTree(taskResponse).get("id").asText();

        mockMvc.perform(delete("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.findAll()).isEmpty();
    }

    @Test
    void deleteTask_shouldReturn404_whenNotExists() throws Exception {
        mockMvc.perform(delete("/api/tasks/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}