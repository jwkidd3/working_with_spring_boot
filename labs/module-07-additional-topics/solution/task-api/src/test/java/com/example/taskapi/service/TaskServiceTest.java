package com.example.taskapi.service;

import com.example.taskapi.dto.CreateTaskRequest;
import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.exception.TaskNotFoundException;
import com.example.taskapi.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task("Test Task", "Test Description", TaskStatus.TODO, TaskPriority.HIGH);
        task.setId(1L);
    }

    @Test
    void findById_WhenExists_ReturnsTask() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // When
        Task result = taskService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Task");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(result.getPriority()).isEqualTo(TaskPriority.HIGH);

        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void findById_WhenNotExists_ThrowsException() {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> taskService.findById(999L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task not found with id: 999");

        verify(taskRepository, times(1)).findById(999L);
    }

    @Test
    void create_WithValidRequest_ReturnsTask() {
        // Given
        CreateTaskRequest request = new CreateTaskRequest(
                "New Task",
                "New Description",
                TaskStatus.TODO,
                TaskPriority.MEDIUM
        );

        Task savedTask = new Task(
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getPriority()
        );
        savedTask.setId(2L);

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // When
        Task result = taskService.create(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("New Task");
        assertThat(result.getDescription()).isEqualTo("New Description");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(result.getPriority()).isEqualTo(TaskPriority.MEDIUM);

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void delete_WhenExists_DeletesSuccessfully() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        doNothing().when(taskRepository).delete(task);

        // When
        taskService.delete(1L);

        // Then
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void delete_WhenNotExists_ThrowsException() {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> taskService.delete(999L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task not found with id: 999");

        verify(taskRepository, times(1)).findById(999L);
        verify(taskRepository, never()).delete(any(Task.class));
    }
}
