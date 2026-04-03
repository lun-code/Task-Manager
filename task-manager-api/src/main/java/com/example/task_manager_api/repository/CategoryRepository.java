package com.example.task_manager_api.repository;

import com.example.task_manager_api.entity.Category;
import com.example.task_manager_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByUser(User user);

    Optional<Category> findByIdAndUser(Long id, User user);
}