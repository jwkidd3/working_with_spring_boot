package com.example.taskapi.config;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Data initializer to populate the database with sample tasks.
 * Runs on application startup.
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
        logger.info("Initializing sample task data...");

        // Create sample tasks in different states
        Task task1 = new Task(
                "Setup Development Environment",
                "Install Java 17, Maven, and IDE for the project",
                TaskStatus.COMPLETED,
                TaskPriority.HIGH
        );
        task1.setDueDate(LocalDateTime.now().minusDays(5));

        Task task2 = new Task(
                "Implement HATEOAS Support",
                "Add Spring HATEOAS dependency and create model assemblers",
                TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH
        );
        task2.setDueDate(LocalDateTime.now().plusDays(2));

        Task task3 = new Task(
                "Write API Documentation",
                "Document all REST endpoints with examples",
                TaskStatus.CREATED,
                TaskPriority.MEDIUM
        );
        task3.setDueDate(LocalDateTime.now().plusDays(7));

        Task task4 = new Task(
                "Add Unit Tests",
                "Write comprehensive unit tests for service layer",
                TaskStatus.CREATED,
                TaskPriority.HIGH
        );
        task4.setDueDate(LocalDateTime.now().plusDays(3));

        Task task5 = new Task(
                "Code Review",
                "Review pull requests from team members",
                TaskStatus.IN_PROGRESS,
                TaskPriority.CRITICAL
        );
        task5.setDueDate(LocalDateTime.now().plusDays(1));

        Task task6 = new Task(
                "Optimize Database Queries",
                "Identify and optimize slow database queries",
                TaskStatus.CREATED,
                TaskPriority.MEDIUM
        );
        task6.setDueDate(LocalDateTime.now().plusDays(10));

        Task task7 = new Task(
                "Update Dependencies",
                "Update all project dependencies to latest versions",
                TaskStatus.CANCELLED,
                TaskPriority.LOW
        );

        Task task8 = new Task(
                "Design New Feature",
                "Create design document for user authentication",
                TaskStatus.COMPLETED,
                TaskPriority.HIGH
        );
        task8.setDueDate(LocalDateTime.now().minusDays(2));

        Task task9 = new Task(
                "Fix Security Vulnerability",
                "Address the security vulnerability found in dependency scan",
                TaskStatus.CREATED,
                TaskPriority.CRITICAL
        );
        task9.setDueDate(LocalDateTime.now().plusHours(12));

        Task task10 = new Task(
                "Team Meeting",
                "Weekly team sync-up meeting",
                TaskStatus.COMPLETED,
                TaskPriority.MEDIUM
        );
        task10.setDueDate(LocalDateTime.now().minusDays(1));

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

        logger.info("Sample data initialized successfully. Total tasks: {}", taskRepository.count());
    }
}
