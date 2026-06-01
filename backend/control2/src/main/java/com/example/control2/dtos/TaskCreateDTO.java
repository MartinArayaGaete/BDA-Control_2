package com.example.control2.dtos;

import com.example.control2.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskCreateDTO {
    @NotBlank(message = "El título es obligatorio")
    private String title;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate dueDate;

    private String description;
    private TaskStatus status;
    private Long userId;

    // Coordenadas
    @NotNull(message = "La ubicación es obligatoria (longitude")
    private Double longitude;

    @NotNull(message = "La ubicación es obligatoria (latitude)")
    private Double latitude;
}

