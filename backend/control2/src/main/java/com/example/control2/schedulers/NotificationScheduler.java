package com.example.control2.schedulers;

import com.example.control2.entities.NotificationEntity;
import com.example.control2.entities.TaskEntity;
import com.example.control2.repositories.NotificationRepository;
import com.example.control2.repositories.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class NotificationScheduler {

    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;

    public NotificationScheduler(TaskRepository taskRepository, NotificationRepository notificationRepository) {
        this.taskRepository = taskRepository;
        this.notificationRepository = notificationRepository;
    }

    // "0 0 8 * * *" (Todos los días a las 8:00 AM)
    // Formato: segundo minuto hora día_mes mes día_semana
    @Scheduled(cron = "0 */2 * * * *")
    @Transactional
    public void checkExpiringTasks() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<TaskEntity> expiringTasks = taskRepository.findTasksNearExpiration(tomorrow);

        for (TaskEntity task : expiringTasks) {
            // Verificamos que siga pendiente en la BD en ese momento
            if ("PENDING".equals(task.getStatus().name()) &&
                    !notificationRepository.existsByTaskIdAndIsReadFalse(task.getId())) {

                NotificationEntity notification = new NotificationEntity();
                notification.setUser(task.getUser());
                notification.setTaskId(task.getId());
                notification.setMessage("¡Atención! Tu tarea '" + task.getTitle() + "' vence mañana (" + task.getDueDate() + ").");

                notificationRepository.save(notification);
            }
        }
    }

}