package com.example.control2.dtos;

import com.example.control2.enums.TaskStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private TaskStatus status;
    private Long userId;
    private String userName;

    // Coordenadas extraídas de la BD
    private double longitude;
    private double latitude;
}