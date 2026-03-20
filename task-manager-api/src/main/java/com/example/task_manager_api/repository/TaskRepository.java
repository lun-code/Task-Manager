package com.example.task_manager_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.task_manager_api.entity.Task;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    // Al heredar de JpaRepository, ya tengo:
    // .save(), .findAll(), .findById(), .deleteById()...
}