package com.example.task_manager_api.specification;

import com.example.task_manager_api.entity.Task;
import org.springframework.data.jpa.domain.Specification;

/**
 * Clase que define los filtros dinámicos para las consultas de Task.
 * Cada método devuelve un filtro (Specification) que se puede combinar con otros.
 * Si el parámetro recibido es null (el usuario no lo mandó), el filtro no se aplica.
 */
public class TaskSpecification {

    /**
     * Filtra tareas por su estado de completado.
     * Equivale a: WHERE completed = true/false
     *
     * @param completed true = solo completadas, false = solo pendientes, null = sin filtro
     */
    public static Specification<Task> hasCompleted(Boolean completed) {
        return (root, query, cb) ->
                // root = la entidad Task, cb.equal construye la condición SQL
                completed == null ? null : cb.equal(root.get("completed"), completed);
    }

    /**
     * Filtra tareas por el id de su categoría.
     * Equivale a: WHERE category_id = ?
     *
     * @param categoryId id de la categoría por la que filtrar, null = sin filtro
     */
    public static Specification<Task> hasCategory(Long categoryId) {
        return (root, query, cb) ->
                // root.get("category").get("id") navega la relación Task -> Category -> id
                categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }
}