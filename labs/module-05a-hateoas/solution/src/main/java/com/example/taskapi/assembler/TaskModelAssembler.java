package com.example.taskapi.assembler;

import com.example.taskapi.controller.TaskController;
import com.example.taskapi.dto.TaskResponse;
import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskStatus;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Assembler to convert Task entities to TaskResponse DTOs with HATEOAS links.
 * Extends RepresentationModelAssemblerSupport for automatic collection handling.
 */
@Component
public class TaskModelAssembler extends RepresentationModelAssemblerSupport<Task, TaskResponse> {

    public TaskModelAssembler() {
        super(TaskController.class, TaskResponse.class);
    }

    /**
     * Converts a Task entity to a TaskResponse DTO with hypermedia links.
     * Adds conditional links based on task status.
     */
    @Override
    public TaskResponse toModel(Task task) {
        TaskResponse taskResponse = new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getDueDate()
        );

        // Self link - link to this specific task
        taskResponse.add(linkTo(methodOn(TaskController.class)
                .getTaskById(task.getId()))
                .withSelfRel());

        // Collection link - link to all tasks
        taskResponse.add(linkTo(methodOn(TaskController.class)
                .getAllTasks(0, 10))
                .withRel("tasks"));

        // Conditional links based on task status
        addStatusBasedLinks(taskResponse, task);

        return taskResponse;
    }

    /**
     * Adds status-based conditional links to the task response.
     * Different actions are available depending on the current task status.
     */
    private void addStatusBasedLinks(TaskResponse taskResponse, Task task) {
        TaskStatus status = task.getStatus();

        switch (status) {
            case CREATED:
                // Can start or cancel a created task
                taskResponse.add(linkTo(methodOn(TaskController.class)
                        .startTask(task.getId()))
                        .withRel("start"));
                taskResponse.add(linkTo(methodOn(TaskController.class)
                        .cancelTask(task.getId()))
                        .withRel("cancel"));
                break;

            case IN_PROGRESS:
                // Can complete or cancel an in-progress task
                taskResponse.add(linkTo(methodOn(TaskController.class)
                        .completeTask(task.getId()))
                        .withRel("complete"));
                taskResponse.add(linkTo(methodOn(TaskController.class)
                        .cancelTask(task.getId()))
                        .withRel("cancel"));
                break;

            case COMPLETED:
                // Can reopen a completed task
                taskResponse.add(linkTo(methodOn(TaskController.class)
                        .reopenTask(task.getId()))
                        .withRel("reopen"));
                break;

            case CANCELLED:
                // Can reopen a cancelled task
                taskResponse.add(linkTo(methodOn(TaskController.class)
                        .reopenTask(task.getId()))
                        .withRel("reopen"));
                break;
        }

        // Update and delete links are always available
        taskResponse.add(linkTo(methodOn(TaskController.class)
                .updateTask(task.getId(), null))
                .withRel("update"));
        taskResponse.add(linkTo(methodOn(TaskController.class)
                .deleteTask(task.getId()))
                .withRel("delete"));
    }

    /**
     * Converts a collection of Tasks to a CollectionModel with links.
     */
    @Override
    public CollectionModel<TaskResponse> toCollectionModel(Iterable<? extends Task> entities) {
        CollectionModel<TaskResponse> taskResponses = super.toCollectionModel(entities);

        // Add self link to the collection
        taskResponses.add(linkTo(methodOn(TaskController.class)
                .getAllTasks(0, 10))
                .withSelfRel());

        return taskResponses;
    }
}
