package com.example.task_manager_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks") // No es necesario, pero por consistencia con "users"
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    private boolean completed = false;

    @CreationTimestamp // Esto rellena la fecha automáticamente al crear la tarea
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) // // Hibernate lanzará error si intentas guardar una tarea sin categoría y Lazy para no cargar la categoría si no la necesitamos
    @JoinColumn(name = "category_id", nullable = false) // Esta será la FK de la tabla task y PostgreSQL pondrá una restricción NOT NULL
    private Category category;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}