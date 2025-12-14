package com.example.taskapi.config;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.repository.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TaskRepository taskRepository;

    public DataInitializer(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public void run(String... args) {
        // Create sample tasks
        createTask("Complete project documentation", "Write comprehensive docs", TaskPriority.HIGH, TaskStatus.TODO, "alice", 7);
        createTask("Review pull requests", "Review team PRs", TaskPriority.MEDIUM, TaskStatus.IN_PROGRESS, "bob", 2);
        createTask("Fix login bug", "Users can't login with SSO", TaskPriority.URGENT, TaskStatus.TODO, "alice", 1);
        createTask("Update dependencies", "Upgrade Spring Boot", TaskPriority.LOW, TaskStatus.TODO, "charlie", 14);
        createTask("Write unit tests", "Increase code coverage", TaskPriority.MEDIUM, TaskStatus.IN_PROGRESS, "bob", 5);
        createTask("Deploy to staging", "Deploy new features", TaskPriority.HIGH, TaskStatus.COMPLETED, "alice", -2);
        createTask("Performance optimization", "Optimize database queries", TaskPriority.MEDIUM, TaskStatus.TODO, "charlie", 10);
        createTask("Security audit", "Review security vulnerabilities", TaskPriority.URGENT, TaskStatus.TODO, "alice", -1);
        createTask("Team meeting prep", "Prepare slides for meeting", TaskPriority.LOW, TaskStatus.COMPLETED, "bob", -3);
        createTask("API documentation", "Document REST endpoints", TaskPriority.MEDIUM, TaskStatus.IN_PROGRESS, "charlie", 3);

        System.out.println("Sample data initialized: " + taskRepository.count() + " tasks created");
    }

    private void createTask(String title, String description, TaskPriority priority, TaskStatus status, String assignee, int dueDaysFromNow) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setStatus(status);
        task.setAssignee(assignee);
        task.setDueDate(LocalDate.now().plusDays(dueDaysFromNow));
        taskRepository.save(task);
    }
}
