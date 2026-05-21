package com.example.control2.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponseDTO {
    private Long id;
    private String message;
    private Long taskId;
    private LocalDateTime createdAt;
}