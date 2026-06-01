package com.example.control2.mappers;

import com.example.control2.dtos.TaskCreateDTO;
import com.example.control2.dtos.TaskResponseDTO;
import com.example.control2.entities.TaskEntity;
import com.example.control2.entities.UserEntity;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    // DTO + User -> Entity (Para CREATE)
    public TaskEntity toEntity(TaskCreateDTO dto, UserEntity user) {
        TaskEntity entity = new TaskEntity();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setDueDate(dto.getDueDate());
        entity.setUser(user);

        // Transformación Espacial segura
        if (dto.getLongitude() != null && dto.getLatitude() != null) {
            Coordinate coordinate = new Coordinate(dto.getLongitude(), dto.getLatitude());
            Point location = geometryFactory.createPoint(coordinate);
            entity.setLocation(location);
        }

        return entity;
    }

    // Entity -> DTO
    public TaskResponseDTO toDTO(TaskEntity entity) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setDueDate(entity.getDueDate());
        dto.setStatus(entity.getStatus());

        // Datos del Usuario
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUserName(entity.getUser().getUsername());
        }

        // Si el Service encontró un sector mediante ST_Contains, lo devolvemos aquí
        if (entity.getSector() != null) {
            dto.setSectorId(entity.getSector().getId());
            dto.setSectorName(entity.getSector().getName());
        }

        // Extracción Espacial
        if (entity.getLocation() != null) {
            dto.setLongitude(entity.getLocation().getX());
            dto.setLatitude(entity.getLocation().getY());
        }

        return dto;
    }

    // Update existing Entity (Para PUT)
    public void updateEntity(TaskCreateDTO dto, TaskEntity entity, UserEntity user) {

        // Solo actualizamos si el dato viene en el DTO
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }

        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }

        if (dto.getDueDate() != null) {
            entity.setDueDate(dto.getDueDate());
        }

        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }

        if (user != null) {
            entity.setUser(user);
        }

        // Actualizar posición solo si envían ambas coordenadas
        if (dto.getLongitude() != null && dto.getLatitude() != null) {
            Coordinate coordinate = new Coordinate(dto.getLongitude(), dto.getLatitude());
            Point location = geometryFactory.createPoint(coordinate);
            entity.setLocation(location);
        }
    }
}