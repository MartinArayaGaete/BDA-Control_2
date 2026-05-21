package com.example.control2.repositories;

import com.example.control2.dtos.TasksForUserPerSectorProjection;
import com.example.control2.dtos.UserSectorStatsProjection;
import com.example.control2.entities.TaskEntity;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    Page<TaskEntity> findByUserId(Long userId, Pageable pageable);

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
     * ¿Cuál es el promedio de distancia de las tareas completadas respecto a la
     * ubicación del usuario?
     */
    @Query(value = """
            SELECT AVG(ST_Distance(CAST(t.location AS geography), CAST(:userLocation AS geography)))
            FROM tasks t
            WHERE t.status = 'COMPLETED'
            """, nativeQuery = true)
    Double getAverageDistanceOfCompletedTasks(@Param("userLocation") org.locationtech.jts.geom.Point userLocation);



    /**
     * Búsqueda Filtra por estado y/o por palabra clave en título o descripción.
     */
    @Query("SELECT t FROM TaskEntity t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<TaskEntity> searchTasks(
            @Param("status") com.example.control2.enums.TaskStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );


    /**
     * ¿Cuántas tareas ha realizado cada usuario por sector?
     */
    @Query(value = """
            SELECT 
                COUNT(t.id) AS taskCount, 
                u.username AS username,
                s.name AS sectorName
            FROM users u
            JOIN tasks t ON t.user_id = u.id
            JOIN sectors s ON ST_Contains(s.geometry, t.location)
            WHERE t.status = 'COMPLETED'
            GROUP BY u.id, u.username, s.id, s.name
            ORDER BY u.username ASC, taskCount DESC
            """, nativeQuery = true)
    List<TasksForUserPerSectorProjection> countTasksForEveryUserPerSector();


    /**
     * Busca todas las tareas pendientes que vencen en una fecha específica
     * y que tienen un usuario asignado.
     */
    @Query(value = """
            SELECT * FROM tasks 
            WHERE status = 'PENDING' 
              AND due_date = :targetDate 
              AND user_id IS NOT NULL
            """, nativeQuery = true)
    List<TaskEntity> findTasksNearExpiration(@Param("targetDate") java.time.LocalDate targetDate);


}