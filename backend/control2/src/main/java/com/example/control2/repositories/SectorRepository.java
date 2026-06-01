package com.example.control2.repositories;

import com.example.control2.dtos.SectorTaskCountProjection;
import com.example.control2.entities.SectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectorRepository extends JpaRepository<SectorEntity, Long> {

    /**
     * ¿Cuál es el sector con más tareas completadas en un radio variable del usuario?
     */
    @Query(value = """
            SELECT s.id, s.name, COUNT(t.id) as completedTasksCount
            FROM sectors s
            JOIN tasks t ON ST_Contains(s.geometry, t.location) 
            WHERE t.status = 'COMPLETED'
              AND ST_DWithin(CAST(s.geometry AS geography), CAST(:userLocation AS geography), :radiusInMeters)
            GROUP BY s.id, s.name
            ORDER BY completedTasksCount DESC
            LIMIT 1
            """, nativeQuery = true)
    SectorTaskCountProjection findTopSectorWithCompletedTasksInRadius(
            @Param("userLocation") org.locationtech.jts.geom.Point userLocation,
            @Param("radiusInMeters") double radiusInMeters
    );


    /**
     * Encuentra el sector en el que cae un punto geográfico específico.
     */
    @Query(value = """
            SELECT * FROM sectors s
            WHERE ST_Contains(s.geometry, :location)
            LIMIT 1
            """, nativeQuery = true)
    SectorEntity findSectorByLocation(@Param("location") org.locationtech.jts.geom.Point location);


    /**
     * ¿En qué sectores geográficos se concentran la mayoría de las tareas
     * pendientes? (utilizando agrupación espacial).
     */
    @Query(value = """
            SELECT s.id, s.name, COUNT(t.id) as taskCount
            FROM sectors s
            LEFT JOIN tasks t ON ST_Contains(s.geometry, t.location) AND t.status = 'PENDING'
            GROUP BY s.id, s.name
            ORDER BY taskCount DESC
            """, nativeQuery = true)
    List<SectorTaskCountProjection> getSpatialConcentrationOfPendingTasks();

    /**
     * Verifica si un nuevo polígono se solapa (intersecta) con algún sector existente.
     */
    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM sectors s
                WHERE ST_Intersects(s.geometry, :newGeometry)
            )
            """, nativeQuery = true)
    boolean existsOverlappingSector(@Param("newGeometry") org.locationtech.jts.geom.Polygon newGeometry);

    /**
     * Verifica si un polígono se solapa con algún sector existente,
     * ignorando el sector que se está editando actualmente.
     */
    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM sectors s
                WHERE ST_Intersects(s.geometry, :newGeometry)
                  AND s.id != :sectorId
            )
            """, nativeQuery = true)
    boolean existsOverlappingSectorExcludingId(
            @Param("newGeometry") org.locationtech.jts.geom.Polygon newGeometry,
            @Param("sectorId") Long sectorId
    );

}