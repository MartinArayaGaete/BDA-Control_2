package com.example.control2.dtos;

import com.example.control2.enums.TaskStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskCreateDTO {
    private String title;
    private String description;
    private LocalDate dueDate;
    private TaskStatus status;
    private Long userId;

    // Coordenadas
    private double longitude;
    private double latitude;
}