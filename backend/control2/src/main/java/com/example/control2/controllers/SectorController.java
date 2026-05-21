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

    // GET ALL SECTORS
    @GetMapping
    public ResponseEntity<List<SectorResponseDTO>> getAllSectors() {
        List<SectorResponseDTO> sectorsList = sectorService.getAllSectors();

        if (sectorsList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(sectorsList);
    }

    // ¿Cuál es el sector con más tareas completadas dentro de un radio de 5 km desde la ubicación del usuario?
    // NOTA: Para cualquier cantidad de kilómetros ingresado
    // GET /api/sectors/stats/top-sector/1?radiusKm=5.0
    @GetMapping("/stats/top-sector/{userId}")
    public ResponseEntity<SectorTaskCountProjection> getTopSector(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "2.0") double radiusKm) {

        SectorTaskCountProjection result = sectorService.getTopSectorInRadius(userId, radiusKm);

        if (result == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(result);
    }


    // ¿En qué sectores se concentran las tareas pendientes?
    // GET /api/sectors/stats/concentration
    @GetMapping("/stats/concentration")
    public ResponseEntity<List<SectorTaskCountProjection>> getConcentration() {
        List<SectorTaskCountProjection> results = sectorService.getConcentrationOfPendingTasks();

        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(results);
    }
}