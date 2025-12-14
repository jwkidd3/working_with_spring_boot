package com.example.taskapi.service;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.UpdateTaskRequest;
import com.example.taskapi.exception.TaskNotFoundException;
import com.example.taskapi.model.Task;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {

    private final Map<Long, Task> taskStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public TaskService() {
        createSampleTasks();
    }

    private void createSampleTasks() {
        CreateTaskRequest task1 = new CreateTaskRequest();
        task1.setTitle("Learn Spring Boot");
        task1.setDescription("Complete the Spring Boot workshop");
        create(task1);

        CreateTaskRequest task2 = new CreateTaskRequest();
        task2.setTitle("Build REST API");
        task2.setDescription("Implement CRUD operations");
        create(task2);
    }

    public List<Task> findAll() {
        return new ArrayList<>(taskStore.values());
    }

    public Task findById(Long id) {
        return Optional.ofNullable(taskStore.get(id))
            .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task create(CreateTaskRequest request) {
        Task task = new Task();
        task.setId(idGenerator.getAndIncrement());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        taskStore.put(task.getId(), task);
        return task;
    }

    public Task update(Long id, UpdateTaskRequest request) {
        Task task = findById(id);

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        task.setUpdatedAt(LocalDateTime.now());
        taskStore.put(id, task);
        return task;
    }

    public void delete(Long id) {
        if (!taskStore.containsKey(id)) {
            throw new TaskNotFoundException(id);
        }
        taskStore.remove(id);
    }
}
