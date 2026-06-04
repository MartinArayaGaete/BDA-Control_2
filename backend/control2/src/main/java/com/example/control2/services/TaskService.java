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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

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

    // CREAR TAREA
    public TaskResponseDTO createTask(TaskCreateDTO dto) {
        // Validaciones básicas
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new RuntimeException("El título de la tarea es obligatorio.");
        }
        if (dto.getDueDate() == null) {
            throw new RuntimeException("La fecha de vencimiento es obligatoria.");
        }
        if (dto.getDueDate().isBefore(java.time.LocalDate.now())) {
            throw new RuntimeException("No puedes crear tareas con fecha de vencimiento en el pasado.");
        }

        // Validar usuario solo si se envió el ID
        UserEntity user = null;
        if (dto.getUserId() != null) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Usuario con ID " + dto.getUserId() + " no encontrado en el sistema."));
        }

        // Mapear y configurar estado
        TaskEntity newTask = taskMapper.toEntity(dto, user);
        newTask.setStatus(TaskStatus.PENDING);

        // Asignar sector (si la ubicación es nula, lanzamos error)
        if (newTask.getLocation() == null) {
            throw new RuntimeException("La ubicación de la tarea es obligatoria para asignarla a un sector.");
        }

        SectorEntity sector = sectorRepository.findSectorByLocation(newTask.getLocation());
        newTask.setSector(sector);

        // Guardar
        return taskMapper.toDTO(taskRepository.save(newTask));
    }


    // OBTENER TAREA POR ID
    public TaskResponseDTO getTaskById(Long id) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea con ID " + id + " no encontrada."));

        return taskMapper.toDTO(task);
    }

    // LEER TODAS LAS TAREAS (PAGINADO)
    public Page<TaskResponseDTO> getAllTasks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return taskRepository.findAll(pageable)
                .map(taskMapper::toDTO);
    }


    public TaskResponseDTO updateTask(Long id, TaskCreateDTO dto) {
        // Validar existencia de la tarea
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea con ID " + id + " no encontrada."));

        // fecha
        if (dto.getDueDate() != null && dto.getDueDate().isBefore(java.time.LocalDate.now())) {
            throw new RuntimeException("No puedes actualizar una tarea con fecha de vencimiento en el pasado.");
        }

        // Validar usuario si se envió
        UserEntity user = null;
        if (dto.getUserId() != null) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Usuario con ID " + dto.getUserId() + " no encontrado."));
        }

        // Mapeo y actualización de entidad
        taskMapper.updateEntity(dto, task, user);

        // Recalcular sector si la ubicación cambió
        if (dto.getLongitude() != null && dto.getLatitude() != null) {
            SectorEntity sector = sectorRepository.findSectorByLocation(task.getLocation());
            task.setSector(sector);
        }

        // Si la tarea estaba expirada, pero le asignaron una nueva fecha válida a futuro, la revivimos a PENDING
        if (task.getStatus() == TaskStatus.EXPIRED && !task.getDueDate().isBefore(java.time.LocalDate.now())) {
            task.setStatus(TaskStatus.PENDING);
        }

        TaskEntity updatedTask = taskRepository.save(task);
        return taskMapper.toDTO(updatedTask);
    }


    // ELIMINAR TAREA
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Tarea no encontrada");
        }
        taskRepository.deleteById(id);
    }

    // MARCAR TAREA COMO COMPLETADA
    public TaskResponseDTO completeTask(Long id, Long userId) {
        //  Buscar la tarea. Si no existe, lanza excepción y corta la ejecución.
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea con ID " + id + " no encontrada."));

        // No completar tareas expiradas
        if (task.getStatus() == TaskStatus.EXPIRED) {
            throw new RuntimeException("Error: No se puede completar una tarea que ya ha expirado.");
        }

        // No re-completar tareas ya listas
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new RuntimeException("Aviso: Esta tarea ya fue marcada como completada anteriormente.");
        }

        // Buscar el usuario que la completó. Si no existe, lanza excepción.
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario con ID " + userId + " no encontrado en el sistema."));

        // Aplicar cambios
        task.setStatus(TaskStatus.COMPLETED);
        task.setUser(user);

        // Guardar y retornar
        TaskEntity savedTask = taskRepository.save(task);
        return taskMapper.toDTO(savedTask);
    }

    // OBTENER TAREA MÁS CERCANA PENDIENTE
    public TaskResponseDTO getClosestTask(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Point userLocation = user.getLocation();
        TaskEntity closestTask = taskRepository.findClosestTaskByStatus(userLocation, TaskStatus.PENDING.name());

        if (closestTask == null) return null;
        return taskMapper.toDTO(closestTask);
    }

    // OBTENER TAREAS POR USUARIO (PAGINADO)
    public Page<TaskResponseDTO> getTasksByUserId(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Usuario no encontrado");
        }

        Pageable pageable = PageRequest.of(page, size);
        return taskRepository.findByUserId(userId, pageable)
                .map(taskMapper::toDTO);
    }

    // OBTENER ESTADÍSTICAS DE USUARIO POR SECTOR
    public List<UserSectorStatsProjection> getUserStatsBySector(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Usuario no encontrado");
        }
        return taskRepository.countCompletedTasksByUserPerSector(userId);
    }

    // OBTENER DISTANCIA PROMEDIO
    public Double getAverageDistanceForUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Double avg = taskRepository.getAverageDistanceOfCompletedTasks(user.getLocation());

        // Si no hay tareas completadas, AVG devuelve NULL.
        return (avg != null) ? avg : 0.0;
    }

    // BÚSQUEDA Y FILTRO (PAGINADO)
    public Page<TaskResponseDTO> searchTasks(TaskStatus status, String keyword, int page, int size) {
        String safeKeyword = (keyword == null) ? "" : keyword;
        Pageable pageable = PageRequest.of(page, size);

        return taskRepository.searchTasks(status, safeKeyword, pageable)
                .map(taskMapper::toDTO);
    }

    // ESTADÍSTICAS: Tareas completadas por cada usuario en cada sector
    public List<TasksForUserPerSectorProjection> getTasksForEveryUserPerSector() {
        return taskRepository.countTasksForEveryUserPerSector();
    }

    // OBTENER TAREAS CERCA DE UN PUNTO (Búsqueda Radial)
    public Page<TaskResponseDTO> getTasksNearPoint(double lng, double lat, double radiusKm, int page, int size) {
        // Crear la fábrica de geometrías espaciales
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

        // Crear el punto central del radar
        Point centerLocation = geometryFactory.createPoint(new Coordinate(lng, lat));

        // PostGIS usa metros para la distancia geográfica
        double radiusMeters = radiusKm * 1000.0;

        // Configurar paginación
        Pageable pageable = PageRequest.of(page, size);

        // Consultar y mapear a DTO (El DTO ya incluirá el estado y el sector)
        return taskRepository.findTasksNearLocation(centerLocation, radiusMeters, pageable)
                .map(taskMapper::toDTO);
    }

    // FILTRAR TAREAS POR ESTADO Y SECTOR (PAGINADO)
    public Page<TaskResponseDTO> getTasksByFilter(TaskStatus status, Long sectorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return taskRepository.filterTasksByStatusAndSector(status, sectorId, pageable)
                .map(taskMapper::toDTO);
    }

    // ASIGNAR TAREA A UN USUARIO (PONER EN PROGRESO)
    public TaskResponseDTO assignTask(Long id, Long userId) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea con ID " + id + " no encontrada."));

        // Validaciones de negocio
        if (task.getStatus() == TaskStatus.EXPIRED) {
            throw new RuntimeException("Error: No se puede asignar una tarea que ya ha expirado.");
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new RuntimeException("Error: Esta tarea ya fue completada.");
        }
        if (task.getStatus() == TaskStatus.IN_PROGRESS && task.getUser() != null && !task.getUser().getId().equals(userId)) {
            throw new RuntimeException("Aviso: Esta tarea ya está siendo realizada por otro usuario.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario con ID " + userId + " no encontrado en el sistema."));

        // Aplicar cambios
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setUser(user);

        // Guardar y retornar
        TaskEntity savedTask = taskRepository.save(task);
        return taskMapper.toDTO(savedTask);
    }
}