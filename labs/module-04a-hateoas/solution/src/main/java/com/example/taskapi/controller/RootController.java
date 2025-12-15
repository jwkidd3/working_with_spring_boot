package com.example.taskapi.controller;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Root API Controller providing API discovery.
 * This is the entry point for the API, providing links to all available resources.
 */
@RestController
@RequestMapping("/api")
public class RootController {

    /**
     * API root endpoint with discovery links.
     * Clients can start here and navigate the entire API through links.
     */
    @GetMapping
    public ResponseEntity<RepresentationModel<?>> root() {
        RepresentationModel<?> root = new RepresentationModel<>();

        // Add self link
        root.add(linkTo(methodOn(RootController.class).root()).withSelfRel());

        // Add link to tasks collection
        root.add(linkTo(methodOn(TaskController.class)
                .getAllTasks(0, 10))
                .withRel("tasks")
                .withTitle("All tasks"));

        // Add links to filter by status
        root.add(linkTo(methodOn(TaskController.class)
                .getTasksByStatus(com.example.taskapi.entity.TaskStatus.CREATED, 0, 10))
                .withRel("tasks-created")
                .withTitle("Tasks with CREATED status"));

        root.add(linkTo(methodOn(TaskController.class)
                .getTasksByStatus(com.example.taskapi.entity.TaskStatus.IN_PROGRESS, 0, 10))
                .withRel("tasks-in-progress")
                .withTitle("Tasks with IN_PROGRESS status"));

        root.add(linkTo(methodOn(TaskController.class)
                .getTasksByStatus(com.example.taskapi.entity.TaskStatus.COMPLETED, 0, 10))
                .withRel("tasks-completed")
                .withTitle("Tasks with COMPLETED status"));

        root.add(linkTo(methodOn(TaskController.class)
                .getTasksByStatus(com.example.taskapi.entity.TaskStatus.CANCELLED, 0, 10))
                .withRel("tasks-cancelled")
                .withTitle("Tasks with CANCELLED status"));

        // Add links to filter by priority
        root.add(linkTo(methodOn(TaskController.class)
                .getTasksByPriority(com.example.taskapi.entity.TaskPriority.HIGH, 0, 10))
                .withRel("tasks-high-priority")
                .withTitle("High priority tasks"));

        root.add(linkTo(methodOn(TaskController.class)
                .getTasksByPriority(com.example.taskapi.entity.TaskPriority.CRITICAL, 0, 10))
                .withRel("tasks-critical")
                .withTitle("Critical priority tasks"));

        return ResponseEntity.ok(root);
    }
}
