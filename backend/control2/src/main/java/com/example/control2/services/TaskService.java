package com.example.control2.services;

import com.example.control2.dtos.TaskCreateDTO;
import com.example.control2.dtos.TaskResponseDTO;
import com.example.control2.dtos.UserSectorStatsProjection;
import com.example.control2.entities.SectorEntity;
import com.example.control2.entities.TaskEntity;
import com.example.control2.entities.UserEntity;
import com.example.control2.enums.TaskStatus;
import com.example.control2.mappers.TaskMapper;
import com.example.control2.repositories.SectorRepository;
import com.example.control2.repositories.TaskRepository;
import com.example.control2.repositories.UserRepository;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private SectorRepository sectorRepository;

    // CREATE
    public TaskResponseDTO createTask(TaskCreateDTO dto) {
        // Validar usuario opcional
        UserEntity user = null;
        if (dto.getUserId() != null) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        // Mapear a entidad
        TaskEntity newTask = taskMapper.toEntity(dto, user);
        newTask.setStatus(TaskStatus.PENDING);

        // Asigna el sector si es que cae en uno
        SectorEntity sector = sectorRepository.findSectorByLocation(newTask.getLocation());
        newTask.setSector(sector);

        // Guardar
        TaskEntity savedTask = taskRepository.save(newTask);
        return taskMapper.toDTO(savedTask);
    }

    // READ ALL
    public List<TaskResponseDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(taskMapper::toDTO)
                .toList();
    }

    // UPDATE
    public TaskResponseDTO updateTask(Long id, TaskCreateDTO dto) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        UserEntity user = dto.getUserId() != null ?
                userRepository.findById(dto.getUserId()).orElse(null) : null;

        taskMapper.updateEntity(dto, task, user);

        TaskEntity updatedTask = taskRepository.save(task);
        return taskMapper.toDTO(updatedTask);
    }

    // DELETE
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Task not found");
        }
        taskRepository.deleteById(id);
    }

    // COMPLETE TASK
    public TaskResponseDTO completeTask(Long id, Long userId) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        task.setStatus(TaskStatus.COMPLETED);
        task.setUser(user);

        TaskEntity savedTask = taskRepository.save(task);
        return taskMapper.toDTO(savedTask);
    }

    // GET CLOSEST
    public TaskResponseDTO getClosestTask(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Point userLocation = user.getLocation();
        TaskEntity closestTask = taskRepository.findClosestTaskByStatus(userLocation, TaskStatus.PENDING.name());

        if (closestTask == null) return null;
        return taskMapper.toDTO(closestTask);
    }

    // GET TASKS BY USER
    public List<TaskResponseDTO> getTasksByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        return taskRepository.findByUserId(userId).stream()
                .map(taskMapper::toDTO)
                .toList();
    }

    // GET USER STATS BY SECTOR
    public List<UserSectorStatsProjection> getUserStatsBySector(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        return taskRepository.countCompletedTasksByUserPerSector(userId);
    }

    // GET AVERAGE DISTANCE
    public Double getAverageDistanceForUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Double avg = taskRepository.getAverageDistanceOfCompletedTasks(user.getLocation());
        return (avg != null) ? avg : 0.0;
    }

    // SEARCH AND FILTER
    public List<TaskResponseDTO> searchTasks(TaskStatus status, String keyword) {
        // Evitamos el error "bytea" de PostgreSQL:
        // Si el keyword es null, pasamos "", lo que generará un LIKE '%%' (trae todo).
        String safeKeyword = (keyword == null) ? "" : keyword;

        return taskRepository.searchTasks(status, safeKeyword).stream()
                .map(taskMapper::toDTO)
                .toList();
    }
}