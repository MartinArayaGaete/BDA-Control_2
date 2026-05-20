package com.example.control2.repositories;

import com.example.control2.dtos.UserSectorStatsProjection;
import com.example.control2.entities.TaskEntity;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    List<TaskEntity> findByUserId(Long userId);

    /**
     * ¿Cuál es la tarea más cercana al usuario (que esté pendiente)?
     */
    @Query(value = """
            SELECT t.* FROM tasks t
            WHERE t.status = :status
            ORDER BY ST_Distance(CAST(t.location AS geography), CAST(:userLocation AS geography)) ASC
            LIMIT 1
            """, nativeQuery = true)
    TaskEntity findClosestTaskByStatus(
            @Param("userLocation") Point userLocation,
            @Param("status") String status
    );

    /**
     * ¿Cuántas tareas ha hecho el usuario por sector?
     * solo si cae físicamente dentro del polígono.
     */
    @Query(value = """
            SELECT s.name AS sectorName, COUNT(t.id) AS taskCount
            FROM tasks t
            JOIN sectors s ON ST_Contains(s.geometry, t.location)
            WHERE t.user_id = :userId 
              AND t.status = 'COMPLETED'
            GROUP BY s.name
            """, nativeQuery = true)
    List<UserSectorStatsProjection> countCompletedTasksByUserPerSector(@Param("userId") Long userId);

    /**
     * Promedio de distancia de las tareas completadas.
     */
    @Query(value = """
            SELECT AVG(ST_Distance(CAST(t.location AS geography), CAST(:userLocation AS geography)))
            FROM tasks t
            WHERE t.status = 'COMPLETED'
            """, nativeQuery = true)
    Double getAverageDistanceOfCompletedTasks(@Param("userLocation") Point userLocation);



    /**
     * Búsqueda Filtra por estado y/o por palabra clave en título o descripción.
     */
    @Query("SELECT t FROM TaskEntity t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<TaskEntity> searchTasks(
            @Param("status") com.example.control2.enums.TaskStatus status,
            @Param("keyword") String keyword
    );
}