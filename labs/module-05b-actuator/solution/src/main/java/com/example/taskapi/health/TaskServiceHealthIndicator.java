package com.example.taskapi.health;

import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.repository.TaskRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Custom health indicator for Task Service
 * Monitors the health of the task management system
 */
@Component
public class TaskServiceHealthIndicator implements HealthIndicator {

    private final TaskRepository taskRepository;

    public TaskServiceHealthIndicator(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Health health() {
        try {
            // Get task counts by status
            long todoCount = taskRepository.countByStatus(TaskStatus.TODO);
            long inProgressCount = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
            long completedCount = taskRepository.countByStatus(TaskStatus.COMPLETED);
            long cancelledCount = taskRepository.countByStatus(TaskStatus.CANCELLED);
            long totalTasks = taskRepository.count();

            // Get overdue tasks
            long overdueCount = taskRepository.findOverdueTasks(LocalDate.now()).size();

            // Build health status
            Health.Builder healthBuilder = Health.up();

            // Add task statistics
            healthBuilder.withDetail("totalTasks", totalTasks)
                    .withDetail("todoTasks", todoCount)
                    .withDetail("inProgressTasks", inProgressCount)
                    .withDetail("completedTasks", completedCount)
                    .withDetail("cancelledTasks", cancelledCount)
                    .withDetail("overdueTasks", overdueCount);

            // Check for warning conditions
            if (overdueCount > 0) {
                healthBuilder.withDetail("warning", "There are " + overdueCount + " overdue task(s)");
            }

            // If there are too many overdue tasks, mark as degraded
            if (overdueCount > 10) {
                healthBuilder.status("DEGRADED")
                        .withDetail("reason", "Too many overdue tasks");
            }

            return healthBuilder.build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}
