package com.example.control2.mappers;

import com.example.control2.dtos.UserCreateDTO;
import com.example.control2.dtos.UserResponseDTO;
import com.example.control2.entities.UserEntity;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    // Mapping: DTO -> Entity
    public UserEntity toEntity(UserCreateDTO dto) {
        UserEntity entity = new UserEntity();
        entity.setUsername(dto.getUsername());
        entity.setPassword(dto.getPassword());

        // Spatial transformation
        Coordinate coordinate = new Coordinate(dto.getLongitude(), dto.getLatitude());
        Point location = geometryFactory.createPoint(coordinate);
        entity.setLocation(location);

        return entity;
    }

    // Mapping: Entity -> DTO
    public UserResponseDTO toDTO(UserEntity entity) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setStatus(entity.getStatus());

        // Spatial extraction (X = Longitude, Y = Latitude)
        if (entity.getLocation() != null) {
            dto.setLongitude(entity.getLocation().getX());
            dto.setLatitude(entity.getLocation().getY());
        }

        return dto;
    }
}