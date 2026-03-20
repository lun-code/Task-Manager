package com.example.task_manager_api.service;

import com.example.task_manager_api.dto.category.CategoryCreateDTO;
import com.example.task_manager_api.dto.category.CategoryPatchDTO;
import com.example.task_manager_api.dto.category.CategoryResponseDTO;
import com.example.task_manager_api.entity.Category;
import com.example.task_manager_api.exception.DataConflictException;
import com.example.task_manager_api.exception.ResourceNotFoundException;
import com.example.task_manager_api.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponseDTO> findAll(){
        return categoryRepository.findAll()
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    public CategoryResponseDTO findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));

        return buildResponse(category);
    }

    public CategoryResponseDTO save(CategoryCreateDTO categoryCreateDTO) {
        Category category = Category.builder()
                .name(categoryCreateDTO.name())
                .build();

        Category savedCategory = categoryRepository.save(category);

        return buildResponse(savedCategory);
    }

    public CategoryResponseDTO patch(Long id, CategoryPatchDTO categoryPatchDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));

        if (categoryPatchDTO.name() != null) {
            category.setName(categoryPatchDTO.name());
        }

        Category savedCategory = categoryRepository.save(category);

        return buildResponse(savedCategory);
    }

    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id " + id);
        }

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
