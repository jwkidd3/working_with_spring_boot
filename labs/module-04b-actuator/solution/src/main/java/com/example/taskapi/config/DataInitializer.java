package com.example.taskapi.config;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Initializes the database with sample task data
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final TaskRepository taskRepository;

    public DataInitializer(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public void run(String... args) {
        logger.info("Initializing database with sample tasks...");

        // Create sample tasks
        Task task1 = new Task(
                "Complete Spring Boot Actuator Lab",
                "Learn how to use Spring Boot Actuator for monitoring and metrics",
                TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH,
                LocalDate.now().plusDays(3)
        );

        Task task2 = new Task(
                "Review Micrometer documentation",
                "Study Micrometer metrics and custom metrics creation",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                LocalDate.now().plusDays(5)
        );

        Task task3 = new Task(
                "Implement custom health indicators",
                "Create custom health indicators for task service monitoring",
                TaskStatus.COMPLETED,
                TaskPriority.HIGH,
                LocalDate.now().minusDays(2)
        );

        Task task4 = new Task(
                "Set up Prometheus integration",
                "Configure Prometheus registry for metrics export",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                LocalDate.now().plusDays(7)
        );

        Task task5 = new Task(
                "Fix database connection issue",
                "Investigate and resolve intermittent database connection timeouts",
                TaskStatus.IN_PROGRESS,
                TaskPriority.CRITICAL,
                LocalDate.now().minusDays(1) // Overdue task
        );

        Task task6 = new Task(
                "Update API documentation",
                "Document all REST endpoints with OpenAPI/Swagger",
                TaskStatus.TODO,
                TaskPriority.LOW,
                LocalDate.now().plusDays(14)
        );

        Task task7 = new Task(
                "Write unit tests for TaskService",
                "Achieve 80% code coverage for service layer",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                LocalDate.now().minusDays(3) // Overdue task
        );

        Task task8 = new Task(
                "Deploy to staging environment",
                "Deploy the latest version to staging for QA testing",
                TaskStatus.COMPLETED,
                TaskPriority.HIGH,
                LocalDate.now().minusDays(5)
        );

        Task task9 = new Task(
                "Optimize database queries",
                "Review and optimize slow queries identified in performance testing",
                TaskStatus.CANCELLED,
                TaskPriority.MEDIUM,
                LocalDate.now().plusDays(10)
        );

        Task task10 = new Task(
                "Security audit",
                "Conduct security audit and fix vulnerabilities",
                TaskStatus.TODO,
                TaskPriority.CRITICAL,
                LocalDate.now().plusDays(2)
        );

        // Save all tasks
        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        taskRepository.save(task4);
        taskRepository.save(task5);
        taskRepository.save(task6);
        taskRepository.save(task7);
        taskRepository.save(task8);
        taskRepository.save(task9);
        taskRepository.save(task10);

        logger.info("Database initialized with {} tasks", taskRepository.count());
        logger.info("Sample data includes {} overdue tasks for health indicator testing",
                taskRepository.findOverdueTasks(LocalDate.now()).size());
    }
}
