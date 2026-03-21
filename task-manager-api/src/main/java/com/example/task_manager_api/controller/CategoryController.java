package com.example.task_manager_api.controller;

import com.example.task_manager_api.dto.category.CategoryCreateDTO;
import com.example.task_manager_api.dto.category.CategoryPatchDTO;
import com.example.task_manager_api.dto.category.CategoryResponseDTO;
import com.example.task_manager_api.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<CategoryResponseDTO> categories = categoryService.findAll();

        return ResponseEntity.ok(categories); // HTTP 200 OK y DTO
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        CategoryResponseDTO category = categoryService.findById(id);

        return ResponseEntity.ok(category);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryCreateDTO categoryCreateDTO) {
        CategoryResponseDTO savedCategory = categoryService.save(categoryCreateDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory); // HTTP 201 Created y DTO
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> patchCategory(@PathVariable Long id, @Valid @RequestBody CategoryPatchDTO categoryPatchDTO) {
        CategoryResponseDTO patchedCategory = categoryService.patch(id, categoryPatchDTO);

        return ResponseEntity.ok(patchedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);

        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}