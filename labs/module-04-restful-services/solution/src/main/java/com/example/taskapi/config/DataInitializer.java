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

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final TaskRepository taskRepository;

    public DataInitializer(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (taskRepository.count() == 0) {
            logger.info("Initializing sample task data...");

            // Create sample tasks
            Task task1 = new Task();
            task1.setTitle("Complete project documentation");
            task1.setDescription("Write comprehensive documentation for the REST API project");
            task1.setPriority(TaskPriority.HIGH);
            task1.setStatus(TaskStatus.TODO);
            task1.setDueDate(LocalDate.now().plusDays(7));

            Task task2 = new Task();
            task2.setTitle("Review pull requests");
            task2.setDescription("Review pending pull requests from team members");
            task2.setPriority(TaskPriority.MEDIUM);
            task2.setStatus(TaskStatus.IN_PROGRESS);
            task2.setDueDate(LocalDate.now().plusDays(2));

            Task task3 = new Task();
            task3.setTitle("Fix authentication bug");
            task3.setDescription("Resolve the JWT token expiration issue");
            task3.setPriority(TaskPriority.URGENT);
            task3.setStatus(TaskStatus.TODO);
            task3.setDueDate(LocalDate.now().plusDays(1));

            Task task4 = new Task();
            task4.setTitle("Update dependencies");
            task4.setDescription("Update all project dependencies to latest stable versions");
            task4.setPriority(TaskPriority.LOW);
            task4.setStatus(TaskStatus.TODO);
            task4.setDueDate(LocalDate.now().plusDays(14));

            Task task5 = new Task();
            task5.setTitle("Write unit tests");
            task5.setDescription("Add unit tests for the service layer");
            task5.setPriority(TaskPriority.HIGH);
            task5.setStatus(TaskStatus.IN_PROGRESS);
            task5.setDueDate(LocalDate.now().plusDays(5));

            Task task6 = new Task();
            task6.setTitle("Deploy to production");
            task6.setDescription("Deploy version 1.0.0 to production environment");
            task6.setPriority(TaskPriority.URGENT);
            task6.setStatus(TaskStatus.COMPLETED);
            task6.setDueDate(LocalDate.now().minusDays(1));

            Task task7 = new Task();
            task7.setTitle("Refactor database queries");
            task7.setDescription("Optimize slow database queries");
            task7.setPriority(TaskPriority.MEDIUM);
            task7.setStatus(TaskStatus.TODO);
            task7.setDueDate(LocalDate.now().plusDays(10));

            Task task8 = new Task();
            task8.setTitle("Setup CI/CD pipeline");
            task8.setDescription("Configure automated testing and deployment");
            task8.setPriority(TaskPriority.HIGH);
            task8.setStatus(TaskStatus.CANCELLED);
            task8.setDueDate(LocalDate.now().minusDays(5));

            Task task9 = new Task();
            task9.setTitle("Design new dashboard");
            task9.setDescription("Create mockups for the new admin dashboard");
            task9.setPriority(TaskPriority.MEDIUM);
            task9.setStatus(TaskStatus.TODO);
            task9.setDueDate(LocalDate.now().plusDays(20));

            Task task10 = new Task();
            task10.setTitle("Conduct code review");
            task10.setDescription("Review code quality and adherence to standards");
            task10.setPriority(TaskPriority.LOW);
            task10.setStatus(TaskStatus.COMPLETED);
            task10.setDueDate(LocalDate.now().minusDays(3));

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

            logger.info("Sample data initialization completed. Created {} tasks", taskRepository.count());
        } else {
            logger.info("Database already contains data. Skipping initialization.");
        }
    }
}
