package com.example.task_manager_api.service;

import com.example.task_manager_api.dto.category.CategoryCreateDTO;
import com.example.task_manager_api.dto.category.CategoryPatchDTO;
import com.example.task_manager_api.dto.category.CategoryResponseDTO;
import com.example.task_manager_api.entity.Category;
import com.example.task_manager_api.entity.User;
import com.example.task_manager_api.exception.DataConflictException;
import com.example.task_manager_api.exception.ResourceNotFoundException;
import com.example.task_manager_api.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public List<CategoryResponseDTO> findAll() {
        User currentUser = getCurrentUser();

        return categoryRepository.findAllByUser(currentUser) // ← solo las del usuario
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    public CategoryResponseDTO findById(Long id) {
        User currentUser = getCurrentUser();

        Category category = categoryRepository.findByIdAndUser(id, currentUser) // ← filtra por usuario
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));

        return buildResponse(category);
    }

    public CategoryResponseDTO save(CategoryCreateDTO categoryCreateDTO) {
        User currentUser = getCurrentUser();

        Category category = Category.builder()
                .name(categoryCreateDTO.name())
                .user(currentUser) // ← asigna el usuario
                .build();

        try {
            Category savedCategory = categoryRepository.save(category);
            return buildResponse(savedCategory);
        } catch (DataIntegrityViolationException ex) {
            throw new DataConflictException("Category with name " + categoryCreateDTO.name() + " already exists");
        }
    }

    public CategoryResponseDTO patch(Long id, CategoryPatchDTO categoryPatchDTO) {
        User currentUser = getCurrentUser();

        Category category = categoryRepository.findByIdAndUser(id, currentUser) // ← filtra por usuario
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));

        if (categoryPatchDTO.name() != null) {
            category.setName(categoryPatchDTO.name());
        }

        try {
            Category savedCategory = categoryRepository.save(category);
            return buildResponse(savedCategory);
        } catch (DataIntegrityViolationException ex) {
            throw new DataConflictException("Category with name " + categoryPatchDTO.name() + " already exists");
        }
    }

    public void delete(Long id) {
        User currentUser = getCurrentUser();

        // Si no existe o no es del usuario → mismo 404
        categoryRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));

        try {
            categoryRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new DataConflictException("You can't delete this category because it is already in use");
        }
    }

    private CategoryResponseDTO buildResponse(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}