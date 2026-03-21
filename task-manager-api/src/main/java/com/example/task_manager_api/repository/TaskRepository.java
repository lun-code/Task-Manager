package com.example.task_manager_api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.task_manager_api.entity.Task;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {
    // Al heredar de JpaRepository, ya tengo:
    // .save(), .findAll(), .findById(), .deleteById()...

    // JpaSpecificationExecutor es una interfaz de Spring que permite hacer búsquedas con filtros dinámicos (los que el usuario manda o no por la URL)

    // Sobreescribimos el método findAll que hereda de JpaSpecificationExecutor añadiéndole el @EntityGraph, así Spring sabe que siempre que usemos ese método debe hacer el JOIN con la categoría.
    @EntityGraph(attributePaths = "category") // Carga la categoría junto con la tarea en una sola consulta
    Page<Task> findAll(Specification<Task> spec, Pageable pageable);
}