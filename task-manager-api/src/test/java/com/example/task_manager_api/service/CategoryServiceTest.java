package com.example.task_manager_api.service;

import com.example.task_manager_api.dto.category.CategoryCreateDTO;
import com.example.task_manager_api.dto.category.CategoryPatchDTO;
import com.example.task_manager_api.dto.category.CategoryResponseDTO;
import com.example.task_manager_api.entity.Category;
import com.example.task_manager_api.exception.DataConflictException;
import com.example.task_manager_api.exception.ResourceNotFoundException;
import com.example.task_manager_api.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    // ========================
    // findAll
    // ========================

    @Test
    void findAll_shouldReturnAllCategories() {
        // GIVEN
        Category category = Category.builder().id(1L).name("Estudio").build();
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        // WHEN
        List<CategoryResponseDTO> result = categoryService.findAll();

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Estudio");
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoCategories() {
        // GIVEN - el repositorio devuelve una lista vacía
        when(categoryRepository.findAll()).thenReturn(List.of());

        // WHEN
        List<CategoryResponseDTO> result = categoryService.findAll();

        // THEN
        assertThat(result).isEmpty();
    }

    // ========================
    // findById
    // ========================

    @Test
    void findById_shouldReturnCategory_whenExists() {
        // GIVEN
        Category category = Category.builder().id(1L).name("Estudio").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // WHEN
        CategoryResponseDTO result = categoryService.findById(1L);

        // THEN
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Estudio");
    }

    @Test
    void findById_shouldThrowException_whenNotExists() {
        // GIVEN
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // THEN
        assertThatThrownBy(() -> categoryService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ========================
    // save
    // ========================

    @Test
    void save_shouldReturnSavedCategory() {
        // GIVEN
        CategoryCreateDTO dto = new CategoryCreateDTO("Estudio");
        Category saved = Category.builder().id(1L).name("Estudio").build();
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        // WHEN
        CategoryResponseDTO result = categoryService.save(dto);

        // THEN
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Estudio");
    }

    @Test
    void save_shouldThrowException_whenNameAlreadyExists() {
        // GIVEN
        CategoryCreateDTO dto = new CategoryCreateDTO("Estudio");
        when(categoryRepository.save(any(Category.class)))
                .thenThrow(DataIntegrityViolationException.class);

        // THEN
        assertThatThrownBy(() -> categoryService.save(dto))
                .isInstanceOf(DataConflictException.class)
                .hasMessageContaining("Estudio");
    }

    // ========================
// patch
// ========================

    @Test
    void patch_shouldReturnUpdatedCategory() {
        // GIVEN
        Category category = Category.builder().id(1L).name("Estudio").build();
        CategoryPatchDTO dto = new CategoryPatchDTO("Trabajo");
        Category updated = Category.builder().id(1L).name("Trabajo").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(updated);

        // WHEN
        CategoryResponseDTO result = categoryService.patch(1L, dto);

        // THEN
        assertThat(result.name()).isEqualTo("Trabajo");
    }

    @Test
    void patch_shouldThrowException_whenNotExists() {
        // GIVEN
        CategoryPatchDTO dto = new CategoryPatchDTO("Trabajo");
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // THEN
        assertThatThrownBy(() -> categoryService.patch(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void patch_shouldThrowException_whenNameAlreadyExists() {
        // GIVEN
        Category category = Category.builder().id(1L).name("Estudio").build();
        CategoryPatchDTO dto = new CategoryPatchDTO("Trabajo");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class)))
                .thenThrow(DataIntegrityViolationException.class);

        // THEN
        assertThatThrownBy(() -> categoryService.patch(1L, dto))
                .isInstanceOf(DataConflictException.class)
                .hasMessageContaining("Trabajo");
    }

    // ========================
    // delete
    // ========================

    @Test
    void delete_shouldDeleteCategory_whenExists() {
        // GIVEN
        when(categoryRepository.existsById(1L)).thenReturn(true);

        // WHEN
        categoryService.delete(1L);

        // THEN - comprobamos que el repositorio fue llamado exactamente una vez
        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_shouldThrowException_whenNotExists() {
        // GIVEN
        when(categoryRepository.existsById(99L)).thenReturn(false);

        // THEN
        assertThatThrownBy(() -> categoryService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_shouldThrowException_whenCategoryIsInUse() {
        // GIVEN
        when(categoryRepository.existsById(1L)).thenReturn(true);
        doThrow(DataIntegrityViolationException.class)
                .when(categoryRepository).deleteById(1L);

        // THEN
        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(DataConflictException.class);
    }
}