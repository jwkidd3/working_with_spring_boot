package com.example.taskapi.controller;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.dto.TaskSearchCriteria;
import com.example.taskapi.dto.UpdateTaskRequest;
import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public Page<Task> getAllTasks(@PageableDefault(size = 20) Pageable pageable) {
        return taskService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public Task getTask(@PathVariable Long id) {
        return taskService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.create(request);
        return ResponseEntity
                .created(URI.create("/api/tasks/" + task.getId()))
                .body(task);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        taskService.delete(id);
    }

    // Search endpoint using Specifications
    @GetMapping("/search")
    public List<Task> searchTasks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) List<TaskPriority> priorities,
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) Boolean overdue) {

        TaskSearchCriteria criteria = new TaskSearchCriteria();
        criteria.setKeyword(keyword);
        criteria.setStatus(status);
        criteria.setPriority(priority);
        criteria.setPriorities(priorities);
        criteria.setAssignee(assignee);
        criteria.setOverdue(overdue);

        return taskService.search(criteria);
    }

    // JPQL query endpoints
    @GetMapping("/by-status/{status}")
    public List<Task> getByStatus(@PathVariable TaskStatus status) {
        return taskService.findByStatus(status);
    }

    @GetMapping("/keyword/{keyword}")
    public List<Task> searchByKeyword(@PathVariable String keyword) {
        return taskService.searchByKeyword(keyword);
    }

    @GetMapping("/overdue")
    public List<Task> getOverdueTasks() {
        return taskService.findOverdueTasks();
    }

    @GetMapping("/stats/by-status")
    public Map<TaskStatus, Long> getTaskStats() {
        return taskService.getTaskCountByStatus();
    }

    // Bulk operations
    @PostMapping("/bulk/update-status")
    public Map<String, Object> bulkUpdateStatus(
            @RequestParam TaskStatus fromStatus,
            @RequestParam TaskStatus toStatus) {
        int count = taskService.bulkUpdateStatus(fromStatus, toStatus);
        return Map.of(
                "updated", count,
                "fromStatus", fromStatus,
                "toStatus", toStatus
        );
    }
}
