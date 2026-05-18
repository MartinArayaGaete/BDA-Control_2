package com.example.control2.controllers;

import com.example.control2.dtos.TaskCreateDTO;
import com.example.control2.dtos.TaskResponseDTO;
import com.example.control2.dtos.UserSectorStatsProjection;
import com.example.control2.services.SectorService;
import com.example.control2.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<TaskResponseDTO> createTask(@RequestBody TaskCreateDTO dto) {
        TaskResponseDTO createdTask = taskService.createTask(dto);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks() {
        List<TaskResponseDTO> tasks = taskService.getAllTasks();

        if (tasks.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(tasks);
    }

    // PUT (Update)
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable Long id, @RequestBody TaskCreateDTO dto) {
        TaskResponseDTO updatedTask = taskService.updateTask(id, dto);
        return ResponseEntity.ok(updatedTask);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<TaskResponseDTO> completeTask(
            @PathVariable Long id,
            @RequestParam Long userId) {
        TaskResponseDTO completedTask = taskService.completeTask(id, userId);
        return ResponseEntity.ok(completedTask);
    }

    // GET TASKS BY USER
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByUser(@PathVariable Long userId) {
        List<TaskResponseDTO> userTasks = taskService.getTasksByUserId(userId);
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

        // Si el promedio es 0 y el service no encontró tareas, devolvemos No Content
        // Nota: avgDistance ya viene como 0.0 desde el Service si es null
        if (avgDistance == null || avgDistance == 0.0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(avgDistance);
    }

}