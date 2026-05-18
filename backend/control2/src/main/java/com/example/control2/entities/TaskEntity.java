package com.example.control2.entities;

import com.example.control2.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point; // Nueva importación
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@Table(name = "tasks")
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // Cada tarea tiene su propia ubicacion (Point)
    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point location;
}