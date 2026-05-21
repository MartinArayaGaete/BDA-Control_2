package com.example.control2.services;

import com.example.control2.dtos.SectorCreateDTO;
import com.example.control2.dtos.SectorResponseDTO;
import com.example.control2.dtos.SectorTaskCountProjection;
import com.example.control2.entities.SectorEntity;
import com.example.control2.entities.UserEntity;
import com.example.control2.mappers.SectorMapper;
import com.example.control2.repositories.SectorRepository;
import com.example.control2.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SectorService {

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private SectorMapper sectorMapper;

    @Autowired
    private UserRepository userRepository;

    public SectorResponseDTO createSector(SectorCreateDTO dto) {
        // Transform to entity using mapper
        SectorEntity newSector = sectorMapper.toEntity(dto);

        // Save to database
        SectorEntity savedSector = sectorRepository.save(newSector);

        // Return mapped response
        return sectorMapper.toDTO(savedSector);
    }

    public Page<SectorResponseDTO> getAllSectors(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return sectorRepository.findAll(pageable)
                .map(sectorMapper::toDTO);
    }

    public SectorTaskCountProjection getTopSectorInRadius(Long userId, double radiusKm) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Convertimos km a metros para PostGIS
        double radiusMeters = radiusKm * 1000;

        return sectorRepository.findTopSectorWithCompletedTasksInRadius(user.getLocation(), radiusMeters);
    }

    public List<SectorTaskCountProjection> getConcentrationOfPendingTasks() {
        return sectorRepository.getSpatialConcentrationOfPendingTasks();
    }
}