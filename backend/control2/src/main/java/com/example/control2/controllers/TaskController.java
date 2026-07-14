package com.example.control2.controllers;

import com.example.control2.dtos.TaskCreateDTO;
import com.example.control2.dtos.TaskResponseDTO;
import com.example.control2.dtos.TasksForUserPerSectorProjection;
import com.example.control2.dtos.UserSectorStatsProjection;
import com.example.control2.entities.TaskEntity;
import com.example.control2.services.SectorService;
import com.example.control2.services.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private SectorService sectorService;

    // POST (Create)
    @PostMapping
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskCreateDTO dto) {
        try {
            TaskResponseDTO createdTask = taskService.createTask(dto);
            return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    // GET ALL
    @GetMapping
    public ResponseEntity<Page<TaskResponseDTO>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<TaskResponseDTO> tasksPage = taskService.getAllTasks(page, size);

        if (tasksPage.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(tasksPage);
    }

    // OBTENER TAREA POR ID
    // GET /api/tasks/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        try {
            TaskResponseDTO task = taskService.getTaskById(id);
            return ResponseEntity.ok(task);
        } catch (RuntimeException e) {
            // Devuelve 404 si la tarea no existe
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // PUT (Update)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskCreateDTO dto) {
        try {
            TaskResponseDTO updatedTask = taskService.updateTask(id, dto);
            return ResponseEntity.ok(updatedTask);
        } catch (RuntimeException e) {
            // Devuelve el mensaje claro del servicio con un 400
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<?> completeTask(
            @PathVariable Long id,
            @RequestParam Long userId) {
        try {
            TaskResponseDTO completedTask = taskService.completeTask(id, userId);
            return ResponseEntity.ok(completedTask);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // GET TASKS BY USER
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<TaskResponseDTO>> getTasksByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<TaskResponseDTO> userTasks = taskService.getTasksByUserId(userId, page, size);

        if (userTasks.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(userTasks);
    }

    // ¿Cuántas tareas ha hecho el usuario por sector?
    // GET /api/tasks/stats/user/1
    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<List<UserSectorStatsProjection>> getUserStats(@PathVariable Long userId) {
        List<UserSectorStatsProjection> stats = taskService.getUserStatsBySector(userId);

        if (stats.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(stats);
    }

    // ¿Cuál es la tarea más cercana al usuario (que esté pendiente)?
    @GetMapping("/closest/{userId}")
    public ResponseEntity<TaskResponseDTO> getClosestTask(@PathVariable Long userId) {
        TaskResponseDTO closestTask = taskService.getClosestTask(userId);

        if (closestTask == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(closestTask);
    }

    //¿Cuál es el promedio de distancia de las tareas completadas respecto a la ubicación del usuario?
    // GET /api/tasks/stats/average-distance/1
    @GetMapping("/stats/average-distance/{userId}")
    public ResponseEntity<Double> getAverageDistance(@PathVariable Long userId) {
        Double avgDistance = taskService.getAverageDistanceForUser(userId);

        //avgDistance viene como 0.0 desde el Service si es null
        if (avgDistance == null || avgDistance == 0.0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(avgDistance);
    }

    // GET /api/tasks/search?status=PENDING&keyword=luz
    @GetMapping("/search")
    public ResponseEntity<Page<TaskResponseDTO>> searchTasks(
            @RequestParam(required = false) com.example.control2.enums.TaskStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<TaskResponseDTO> resultsPage = taskService.searchTasks(status, keyword, page, size);

        if (resultsPage.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(resultsPage);
    }


    // ¿Cuántas tareas ha realizado cada usuario por sector?
    // GET /api/tasks/stats/users-sectors
    @GetMapping("/stats/users-sectors")
    public ResponseEntity<List<TasksForUserPerSectorProjection>> getTasksForEveryUserPerSector() {
        List<TasksForUserPerSectorProjection> stats = taskService.getTasksForEveryUserPerSector();

        if (stats.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(stats);
    }

    // buscar tareas al rededor de un punto
    // GET /api/tasks/nearby?lng=-70.665&lat=-33.528&radiusKm=5.0
    @GetMapping("/nearby")
    public ResponseEntity<Page<TaskResponseDTO>> getTasksNearby(
            @RequestParam double lng,
            @RequestParam double lat,
            @RequestParam(defaultValue = "5.0") double radiusKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<TaskResponseDTO> nearbyTasks = taskService.getTasksNearPoint(lng, lat, radiusKm, page, size);

        if (nearbyTasks.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(nearbyTasks);
    }


    // GET /api/tasks/filter?status=PENDING&sectorId=1&page=0&size=10
    @GetMapping("/filter")
    public ResponseEntity<?> filterTasks(
            @RequestParam(required = false) com.example.control2.enums.TaskStatus status,
            @RequestParam(required = false) Long sectorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<TaskResponseDTO> filteredTasks = taskService.getTasksByFilter(status, sectorId, page, size);

            if (filteredTasks.isEmpty()) {
                return ResponseEntity.noContent().build(); // 204 si no hay coincidencias
            }

            return ResponseEntity.ok(filteredTasks);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    // PATCH /api/tasks/{id}/assign?userId=1
    @PatchMapping("/{id}/assign")
    public ResponseEntity<?> assignTask(
            @PathVariable Long id,
            @RequestParam Long userId) {
        try {
            TaskResponseDTO assignedTask = taskService.assignTask(id, userId);
            return ResponseEntity.ok(assignedTask);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


}