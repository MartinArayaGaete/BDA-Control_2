package com.example.control2.services;

import com.example.control2.dtos.SectorCreateDTO;
import com.example.control2.dtos.SectorResponseDTO;
import com.example.control2.dtos.SectorTaskCountProjection;
import com.example.control2.entities.SectorEntity;
import com.example.control2.entities.UserEntity;
import com.example.control2.mappers.SectorMapper;
import com.example.control2.repositories.SectorRepository;
import com.example.control2.repositories.TaskRepository;
import com.example.control2.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.List;

@Service
public class SectorService {

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private SectorMapper sectorMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    public SectorResponseDTO createSector(SectorCreateDTO dto) {
        // Transformar el DTO a entidad usando el mapper
        SectorEntity newSector = sectorMapper.toEntity(dto);

        // Comprobar si se solapa con algún sector existente
        if (sectorRepository.existsOverlappingSector(newSector.getGeometry())) {
            throw new RuntimeException("Error: El terreno dibujado se solapa con un sector ya existente. Por favor, ajusta los límites.");
        }

        // Guardar en la base de datos
        SectorEntity savedSector = sectorRepository.save(newSector);

        // Retornar la respuesta mapeada
        return sectorMapper.toDTO(savedSector);
    }

    public Page<SectorResponseDTO> getAllSectors(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return sectorRepository.findAll(pageable)
                .map(sectorMapper::toDTO);
    }

    public SectorTaskCountProjection getTopSectorInRadius(Long userId, double radiusKm) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Convertimos km a metros para PostGIS
        double radiusMeters = radiusKm * 1000;

        return sectorRepository.findTopSectorWithCompletedTasksInRadius(user.getLocation(), radiusMeters);
    }

    public List<SectorTaskCountProjection> getConcentrationOfPendingTasks() {
        return sectorRepository.getSpatialConcentrationOfPendingTasks();
    }

    public SectorResponseDTO getSectorByPoint(double lng, double lat) {
        // Crear la fábrica de geometrías con SRID 4326 (WGS 84, estándar GPS)
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

        // Crear el punto espacial
        Point clickLocation = geometryFactory.createPoint(new Coordinate(lng, lat));

        // Buscar en la base de datos
        SectorEntity sector = sectorRepository.findSectorByLocation(clickLocation);

        // Si no cae en ningún sector, retornamos null para que el controlador decida
        if (sector == null) {
            return null;
        }

        // Retornamos el DTO con la información
        return sectorMapper.toDTO(sector);
    }

    /**
     * Obtiene un sector específico por su ID.
     */
    public SectorResponseDTO getSectorById(Long id) {
        SectorEntity sector = sectorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sector no encontrado con el ID: " + id));

        return sectorMapper.toDTO(sector);
    }

    public SectorResponseDTO updateSector(Long id, SectorCreateDTO dto) {
        // Buscar el sector existente
        SectorEntity existingSector = sectorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sector no encontrado con el ID: " + id));

        // Mapear el DTO a una entidad temporal
        SectorEntity updatedData = sectorMapper.toEntity(dto);

        // Validar solapamiento (excluyendo al sector actual)
        if (sectorRepository.existsOverlappingSectorExcludingId(updatedData.getGeometry(), id)) {
            throw new RuntimeException("Error: La nueva forma del terreno se solapa con otro sector existente.");
        }

        // Actualizar los datos
        existingSector.setName(updatedData.getName());
        existingSector.setGeometry(updatedData.getGeometry());

        // Guardar en la base de datos
        SectorEntity savedSector = sectorRepository.save(existingSector);

        return sectorMapper.toDTO(savedSector);
    }

    public void deleteSector(Long id) {
        // Verificar que el sector existe
        if (!sectorRepository.existsById(id)) {
            throw new RuntimeException("Sector no encontrado con el ID: " + id);
        }

        // Verificar que no tenga tareas asociadas para proteger la integridad relacional
        if (taskRepository.existsBySectorId(id)) {
            throw new RuntimeException("No se puede eliminar el sector porque tiene tareas registradas. Edita el sector en lugar de eliminarlo.");
        }

        // Eliminar el sector
        sectorRepository.deleteById(id);
    }
}