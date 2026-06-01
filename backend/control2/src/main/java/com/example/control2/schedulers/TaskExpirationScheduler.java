package com.example.control2.schedulers;

import com.example.control2.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TaskExpirationScheduler {

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Se ejecuta cada 2 minutos
     * Marca como expirado a las tareas
     * */
    @Scheduled(cron = "0 */2 * * * *")
    public void markExpiredTasks() {
        // Obtenemos la fecha de hoy
        LocalDate today = LocalDate.now();

        // update masivo directo en Postgres
        int updatedTasksCount = taskRepository.expirePastDueTasks(today);

        if (updatedTasksCount > 0) {
            System.out.println("marcadas " + updatedTasksCount + " tareas como EXPIRED.");
        }
    }
}