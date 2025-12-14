package com.example.taskapi.config;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.repository.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(TaskRepository repository) {
        return args -> {
            // Create sample tasks for testing the API
            Task task1 = new Task();
            task1.setTitle("Complete API documentation");
            task1.setDescription("Add OpenAPI annotations to all endpoints");
            task1.setPriority(TaskPriority.HIGH);
            task1.setStatus(TaskStatus.IN_PROGRESS);
            task1.setDueDate(LocalDate.now().plusDays(7));
            repository.save(task1);

            Task task2 = new Task();
            task2.setTitle("Review pull requests");
            task2.setDescription("Review and merge pending PRs from team");
            task2.setPriority(TaskPriority.MEDIUM);
            task2.setStatus(TaskStatus.PENDING);
            task2.setDueDate(LocalDate.now().plusDays(2));
            repository.save(task2);

            Task task3 = new Task();
            task3.setTitle("Setup CI/CD pipeline");
            task3.setDescription("Configure GitHub Actions for automated testing and deployment");
            task3.setPriority(TaskPriority.HIGH);
            task3.setStatus(TaskStatus.PENDING);
            task3.setDueDate(LocalDate.now().plusDays(14));
            repository.save(task3);

            Task task4 = new Task();
            task4.setTitle("Update dependencies");
            task4.setDescription("Update Spring Boot and other dependencies to latest versions");
            task4.setPriority(TaskPriority.LOW);
            task4.setStatus(TaskStatus.COMPLETED);
            task4.setDueDate(LocalDate.now().minusDays(1));
            repository.save(task4);

            System.out.println("Sample data initialized - 4 tasks created");
            System.out.println("Access Swagger UI at: http://localhost:8080/swagger-ui.html");
            System.out.println("Access OpenAPI spec at: http://localhost:8080/api-docs");
        };
    }
}
