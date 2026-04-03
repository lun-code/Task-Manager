package com.example.task_manager_api.service;

import com.example.task_manager_api.dto.category.CategoryCreateDTO;
import com.example.task_manager_api.dto.category.CategoryPatchDTO;
import com.example.task_manager_api.dto.category.CategoryResponseDTO;
import com.example.task_manager_api.entity.Category;
import com.example.task_manager_api.entity.User;
import com.example.task_manager_api.exception.DataConflictException;
import com.example.task_manager_api.exception.ResourceNotFoundException;
import com.example.task_manager_api.repository.CategoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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
    void findAll_shouldReturnAllCategories() {
        // GIVEN
        Category category = Category.builder().id(1L).name("Estudio").user(mockUser).build();
        when(categoryRepository.findAllByUser(mockUser)).thenReturn(List.of(category));

        // WHEN
        List<CategoryResponseDTO> result = categoryService.findAll();

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Estudio");
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoCategories() {
        // GIVEN
        when(categoryRepository.findAllByUser(mockUser)).thenReturn(List.of());

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
        Category category = Category.builder().id(1L).name("Estudio").user(mockUser).build();
        when(categoryRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.of(category));

        // WHEN
        CategoryResponseDTO result = categoryService.findById(1L);

        // THEN
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Estudio");
    }

    @Test
    void findById_shouldThrowException_whenNotExists() {
        // GIVEN
        when(categoryRepository.findByIdAndUser(99L, mockUser)).thenReturn(Optional.empty());

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
        Category saved = Category.builder().id(1L).name("Estudio").user(mockUser).build();
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
        Category category = Category.builder().id(1L).name("Estudio").user(mockUser).build();
        CategoryPatchDTO dto = new CategoryPatchDTO("Trabajo");
        Category updated = Category.builder().id(1L).name("Trabajo").user(mockUser).build();
        when(categoryRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.of(category));
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
        when(categoryRepository.findByIdAndUser(99L, mockUser)).thenReturn(Optional.empty());

        // THEN
        assertThatThrownBy(() -> categoryService.patch(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void patch_shouldThrowException_whenNameAlreadyExists() {
        // GIVEN
        Category category = Category.builder().id(1L).name("Estudio").user(mockUser).build();
        CategoryPatchDTO dto = new CategoryPatchDTO("Trabajo");
        when(categoryRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.of(category));
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
        Category category = Category.builder().id(1L).name("Estudio").user(mockUser).build();
        when(categoryRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.of(category));

        // WHEN
        categoryService.delete(1L);

        // THEN
        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_shouldThrowException_whenNotExists() {
        // GIVEN
        when(categoryRepository.findByIdAndUser(99L, mockUser)).thenReturn(Optional.empty());

        // THEN
        assertThatThrownBy(() -> categoryService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_shouldThrowException_whenCategoryIsInUse() {
        // GIVEN
        Category category = Category.builder().id(1L).name("Estudio").user(mockUser).build();
        when(categoryRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.of(category));
        doThrow(DataIntegrityViolationException.class)
                .when(categoryRepository).deleteById(1L);

        // THEN
        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(DataConflictException.class);
    }
}