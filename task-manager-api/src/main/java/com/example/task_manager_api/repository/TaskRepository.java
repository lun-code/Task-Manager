package com.example.task_manager_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.task_manager_api.entity.Task;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    // Al heredar de JpaRepository, ya tengo:
    // .save(), .findAll(), .findById(), .deleteById()...

    @Query("SELECT t FROM Task t JOIN FETCH t.category")
    List<Task> findAllWithCategory();
}