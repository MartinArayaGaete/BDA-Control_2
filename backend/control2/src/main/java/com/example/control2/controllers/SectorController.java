package com.example.control2.controllers;

import com.example.control2.dtos.SectorCreateDTO;
import com.example.control2.dtos.SectorResponseDTO;
import com.example.control2.dtos.SectorTaskCountProjection;
import com.example.control2.services.SectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<?> createSector(@RequestBody SectorCreateDTO dto) {
        try {
            SectorResponseDTO response = sectorService.createSector(dto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Devuelve un error 400 Bad Request con el mensaje descriptivo para el frontend
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // GET ALL SECTORS
    @GetMapping
    public ResponseEntity<Page<SectorResponseDTO>> getAllSectors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<SectorResponseDTO> sectorsPage = sectorService.getAllSectors(page, size);

        if (sectorsPage.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(sectorsPage);
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

    // IDENTIFICAR SECTOR POR PUNTO (CLIC EN EL MAPA)
    // GET /api/sectors/point?lng=-70.665&lat=-33.528
    @GetMapping("/point")
    public ResponseEntity<?> getSectorByPoint(
            @RequestParam double lng,
            @RequestParam double lat) {

        SectorResponseDTO sector = sectorService.getSectorByPoint(lng, lat);

        if (sector == null) {
            // El frontend necesita saber explícitamente que no hay sector ahí
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("El punto seleccionado no pertenece a ningún sector registrado.");
        }

        return ResponseEntity.ok(sector);
    }

    // OBTENER SECTOR POR ID
    // GET /api/sectors/1
    @GetMapping("/{id}")
    public ResponseEntity<?> getSectorById(@PathVariable Long id) {
        try {
            SectorResponseDTO sector = sectorService.getSectorById(id);
            return ResponseEntity.ok(sector);
        } catch (RuntimeException e) {
            // Devuelve un error 404 si no encuentra el ID
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // PUT /api/sectors/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSector(@PathVariable Long id, @RequestBody SectorCreateDTO dto) {
        try {
            SectorResponseDTO updatedSector = sectorService.updateSector(id, dto);
            return ResponseEntity.ok(updatedSector);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // DELETE /api/sectors/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSector(@PathVariable Long id) {
        try {
            sectorService.deleteSector(id);
            return ResponseEntity.noContent().build(); // 204 No Content (Éxito sin cuerpo de respuesta)
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409 Conflict si hay tareas asociadas
        }
    }


}