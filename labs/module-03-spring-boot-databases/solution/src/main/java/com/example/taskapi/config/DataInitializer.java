package com.example.taskapi.config;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.repository.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(TaskRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                Task task1 = new Task("Learn Spring Boot", "Complete the Spring Boot workshop");
                task1.setPriority(TaskPriority.HIGH);
                task1.setDueDate(LocalDateTime.now().plusDays(7));

                Task task2 = new Task("Build REST API", "Implement CRUD operations");
                task2.setStatus(TaskStatus.IN_PROGRESS);
                task2.setPriority(TaskPriority.HIGH);

                Task task3 = new Task("Write unit tests", "Add comprehensive test coverage");
                task3.setPriority(TaskPriority.MEDIUM);
                task3.setDueDate(LocalDateTime.now().plusDays(14));

                Task task4 = new Task("Deploy to production", "Set up CI/CD pipeline");
                task4.setPriority(TaskPriority.LOW);
                task4.setDueDate(LocalDateTime.now().plusDays(30));

                Task task5 = new Task("Review code", "Peer review pending PRs");
                task5.setStatus(TaskStatus.COMPLETED);
                task5.setPriority(TaskPriority.MEDIUM);

                Task task6 = new Task("Overdue task example", "This task is overdue");
                task6.setDueDate(LocalDateTime.now().minusDays(1));
                task6.setPriority(TaskPriority.URGENT);

                repository.save(task1);
                repository.save(task2);
                repository.save(task3);
                repository.save(task4);
                repository.save(task5);
                repository.save(task6);

                System.out.println("Sample data initialized!");
            }
        };
    }
}
