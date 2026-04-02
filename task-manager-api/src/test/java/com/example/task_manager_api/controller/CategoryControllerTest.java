package com.example.task_manager_api.controller;

import com.example.task_manager_api.config.TestSecurityConfig;
import com.example.task_manager_api.dto.category.CategoryCreateDTO;
import com.example.task_manager_api.dto.category.CategoryResponseDTO;
import com.example.task_manager_api.exception.ResourceNotFoundException;
import com.example.task_manager_api.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(TestSecurityConfig.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    // ========================
    // GET /api/categories
    // ========================

    @Test
    @WithMockUser
    void getAllCategories_shouldReturn200WithList() throws Exception {
        // GIVEN
        CategoryResponseDTO category = CategoryResponseDTO.builder().id(1L).name("Estudio").build();
        when(categoryService.findAll()).thenReturn(List.of(category));

        // WHEN + THEN
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Estudio"));
    }

    // ========================
    // GET /api/categories/{id}
    // ========================

    @Test
    @WithMockUser
    void getCategoryById_shouldReturn200_whenExists() throws Exception {
        // GIVEN
        CategoryResponseDTO category = CategoryResponseDTO.builder().id(1L).name("Estudio").build();
        when(categoryService.findById(1L)).thenReturn(category);

        // WHEN + THEN
        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Estudio"));
    }

    @Test
    @WithMockUser
    void getCategoryById_shouldReturn404_whenNotExists() throws Exception {
        // GIVEN
        when(categoryService.findById(99L)).thenThrow(new ResourceNotFoundException("Category not found with id 99"));

        // WHEN + THEN
        mockMvc.perform(get("/api/categories/99"))
                .andExpect(status().isNotFound());
    }

    // ========================
    // POST /api/categories
    // ========================

    @Test
    @WithMockUser
    void createCategory_shouldReturn201_whenValid() throws Exception {
        // GIVEN
        CategoryCreateDTO dto = new CategoryCreateDTO("Estudio");
        CategoryResponseDTO response = CategoryResponseDTO.builder().id(1L).name("Estudio").build();
        when(categoryService.save(any(CategoryCreateDTO.class))).thenReturn(response);

        // WHEN + THEN
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Estudio"));
    }

    @Test
    @WithMockUser
    void createCategory_shouldReturn400_whenNameIsBlank() throws Exception {
        // GIVEN
        CategoryCreateDTO dto = new CategoryCreateDTO("");

        // WHEN + THEN
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ========================
    // PATCH /api/categories/{id}
    // ========================

    @Test
    @WithMockUser
    void patchCategory_shouldReturn200_whenValid() throws Exception {
        // GIVEN
        CategoryResponseDTO response = CategoryResponseDTO.builder().id(1L).name("Trabajo").build();
        when(categoryService.patch(eq(1L), any())).thenReturn(response);

        // WHEN + THEN
        mockMvc.perform(patch("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Trabajo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Trabajo"));
    }

    // ========================
    // DELETE /api/categories/{id}
    // ========================

    @Test
    @WithMockUser
    void deleteCategory_shouldReturn204_whenExists() throws Exception {
        // GIVEN
        doNothing().when(categoryService).delete(1L);

        // WHEN + THEN
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteCategory_shouldReturn404_whenNotExists() throws Exception {
        // GIVEN
        doThrow(new ResourceNotFoundException("Category not found with id 99"))
                .when(categoryService).delete(99L);

        // WHEN + THEN
        mockMvc.perform(delete("/api/categories/99"))
                .andExpect(status().isNotFound());
    }
}