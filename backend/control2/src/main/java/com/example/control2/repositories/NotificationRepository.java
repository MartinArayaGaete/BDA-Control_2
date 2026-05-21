package com.example.control2.repositories;

import com.example.control2.entities.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    // Buscar notificaciones no leídas de un usuario
    List<NotificationEntity> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // Verificar si ya avisamos de esta tarea
    boolean existsByTaskIdAndIsReadFalse(Long taskId);

    // Buscamos si existe una notificación pendiente para esta tarea específica
    Optional<NotificationEntity> findByTaskIdAndIsReadFalse(Long taskId);
}