package com.example.control2.controllers;

import com.example.control2.dtos.SectorCreateDTO;
import com.example.control2.dtos.SectorResponseDTO;
import com.example.control2.dtos.SectorTaskCountProjection;
import com.example.control2.services.SectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sectors")
public class SectorController {

    @Autowired
    private SectorService sectorService;

    @PostMapping
    public ResponseEntity<SectorResponseDTO> createSector(@RequestBody SectorCreateDTO dto) {
        SectorResponseDTO response = sectorService.createSector(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SectorResponseDTO>> getAllSectors() {
        List<SectorResponseDTO> sectorsList = sectorService.getAllSectors();
        return ResponseEntity.ok(sectorsList);
    }

    // ¿Cuál es el sector con más tareas completadas en un radio de 2 kilómetros del usuario?
    @GetMapping("/stats/top-sector/{userId}")
    public ResponseEntity<SectorTaskCountProjection> getTopSector(@PathVariable Long userId) {
        SectorTaskCountProjection result = sectorService.getTopSectorInRadius(userId);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.noContent().build();
    }
}