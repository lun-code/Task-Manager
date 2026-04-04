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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        taskRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        RegisterUserDto registerDto = new RegisterUserDto("test@test.com", "password123", "Test User");
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)));

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginUserDto("test@test.com", "password123"))))
                .andReturn().getResponse().getContentAsString();

        token = objectMapper.readTree(loginResponse).get("token").asText();
    }

    // ========================
    // GET /api/categories
    // ========================

    @Test
    void getAllCategories_shouldReturn200WithEmptyList() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAllCategories_shouldReturn200WithCategories() throws Exception {
        mockMvc.perform(post("/api/categories")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Estudio\"}"));

        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Estudio"));
    }

    @Test
    void getAllCategories_shouldReturn403_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isForbidden());
    }

    // ========================
    // GET /api/categories/{id}
    // ========================

    @Test
    void getCategoryById_shouldReturn200_whenExists() throws Exception {
        String categoryResponse = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Estudio\"}"))
                .andReturn().getResponse().getContentAsString();

        Long categoryId = objectMapper.readTree(categoryResponse).get("id").asLong();

        mockMvc.perform(get("/api/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Estudio"));
    }

    @Test
    void getCategoryById_shouldReturn404_whenNotExists() throws Exception {
        mockMvc.perform(get("/api/categories/9999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ========================
    // POST /api/categories
    // ========================

    @Test
    void createCategory_shouldReturn201AndPersist() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Estudio\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Estudio"))
                .andExpect(jsonPath("$.id").isNumber());

        assertThat(categoryRepository.findAll()).hasSize(1);
    }

    @Test
    void createCategory_shouldReturn400_whenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_shouldReturn409_whenNameAlreadyExists() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Estudio\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Estudio\"}"))
                .andExpect(status().isConflict());
    }

    // ========================
    // PATCH /api/categories/{id}
    // ========================

    @Test
    void patchCategory_shouldReturn200AndUpdate() throws Exception {
        String categoryResponse = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Estudio\"}"))
                .andReturn().getResponse().getContentAsString();

        Long categoryId = objectMapper.readTree(categoryResponse).get("id").asLong();

        mockMvc.perform(patch("/api/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Trabajo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Trabajo"));
    }

    @Test
    void patchCategory_shouldReturn404_whenNotExists() throws Exception {
        mockMvc.perform(patch("/api/categories/9999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Trabajo\"}"))
                .andExpect(status().isNotFound());
    }

    // ========================
    // DELETE /api/categories/{id}
    // ========================

    @Test
    void deleteCategory_shouldReturn204AndRemoveFromDatabase() throws Exception {
        String categoryResponse = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Estudio\"}"))
                .andReturn().getResponse().getContentAsString();

        Long categoryId = objectMapper.readTree(categoryResponse).get("id").asLong();

        mockMvc.perform(delete("/api/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(categoryRepository.findAll()).isEmpty();
    }

    @Test
    void deleteCategory_shouldReturn404_whenNotExists() throws Exception {
        mockMvc.perform(delete("/api/categories/9999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory_shouldReturn409_whenCategoryIsInUse() throws Exception {
        String categoryResponse = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Estudio\"}"))
                .andReturn().getResponse().getContentAsString();

        Long categoryId = objectMapper.readTree(categoryResponse).get("id").asLong();

        // Creamos una tarea que usa la categoría
        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Tarea 1\", \"categoryId\": " + categoryId + "}"));

        // Intentamos borrar la categoría en uso
        mockMvc.perform(delete("/api/categories/" + categoryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }
}