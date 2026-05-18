package com.example.control2.mappers;

import com.example.control2.dtos.SectorCreateDTO;
import com.example.control2.dtos.SectorResponseDTO;
import com.example.control2.entities.SectorEntity;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Component;

@Component
public class SectorMapper {

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private final WKTReader wktReader = new WKTReader(geometryFactory);

    // Mapping: DTO -> Entity
    public SectorEntity toEntity(SectorCreateDTO dto) {
        SectorEntity entity = new SectorEntity();
        entity.setName(dto.getName());

        try {
            Polygon polygon = (Polygon) wktReader.read(dto.getWkt());
            polygon.setSRID(4326);
            entity.setGeometry(polygon);
        } catch (ParseException e) {
            throw new RuntimeException("Error reading WKT coordinates for the sector", e);
        }

        return entity;
    }

    // Mapping: Entity -> DTO
    public SectorResponseDTO toDTO(SectorEntity entity) {
        SectorResponseDTO dto = new SectorResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());

        if (entity.getGeometry() != null) {
            dto.setWkt(entity.getGeometry().toText());
        }

        return dto;
    }
}