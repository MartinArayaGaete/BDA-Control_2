package com.example.control2.controllers;

import com.example.control2.dtos.NotificationResponseDTO;
import com.example.control2.entities.NotificationEntity;
import com.example.control2.repositories.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // OBTENER NOTIFICACIONES DETALLADAS
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponseDTO>> getActiveNotifications(@PathVariable Long userId) {
        List<NotificationEntity> notifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        if (notifications.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // Mapeamos
        List<NotificationResponseDTO> dtos = notifications.stream()
                .map(n -> {
                    NotificationResponseDTO dto = new NotificationResponseDTO();
                    dto.setId(n.getId());
                    dto.setMessage(n.getMessage());
                    dto.setTaskId(n.getTaskId());
                    dto.setCreatedAt(n.getCreatedAt());
                    return dto;
                })
                .toList();

        return ResponseEntity.ok(dtos);
    }

    // MARCAR COMO LEÍDA
    // PATCH /api/notifications/5/read
    @PatchMapping("/task/{taskId}/read")
    public ResponseEntity<Void> markAsReadByTaskId(@PathVariable Long taskId) {
        // Buscamos la notificación activa para esa tarea
        NotificationEntity notification = notificationRepository.findByTaskIdAndIsReadFalse(taskId)
                .orElseThrow(() -> new RuntimeException("No hay notificaciones pendientes para la tarea: " + taskId));

        notification.setRead(true); // Marcamos como leída
        notificationRepository.save(notification);

        return ResponseEntity.ok().build();
    }
}