package com.example.control2.services;

import com.example.control2.dtos.TaskCreateDTO;
import com.example.control2.dtos.TaskResponseDTO;
import com.example.control2.dtos.TasksForUserPerSectorProjection;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public Page<TaskResponseDTO> getAllTasks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return taskRepository.findAll(pageable)
                .map(taskMapper::toDTO);
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
    public Page<TaskResponseDTO> getTasksByUserId(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        Pageable pageable = PageRequest.of(page, size);
        return taskRepository.findByUserId(userId, pageable)
                .map(taskMapper::toDTO);
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

        // Si no hay tareas completadas, AVG devuelve NULL.
        return (avg != null) ? avg : 0.0;
    }

    // SEARCH AND FILTER
    public Page<TaskResponseDTO> searchTasks(TaskStatus status, String keyword, int page, int size) {
        String safeKeyword = (keyword == null) ? "" : keyword;
        Pageable pageable = PageRequest.of(page, size);

        return taskRepository.searchTasks(status, safeKeyword, pageable)
                .map(taskMapper::toDTO);
    }

    // STATS: Tareas completadas por cada usuario en cada sector
    public List<TasksForUserPerSectorProjection> getTasksForEveryUserPerSector() {
        return taskRepository.countTasksForEveryUserPerSector();
    }



}