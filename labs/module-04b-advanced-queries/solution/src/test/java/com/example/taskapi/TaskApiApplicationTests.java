package com.example.taskapi;

import com.example.taskapi.entity.Task;
import com.example.taskapi.entity.TaskPriority;
import com.example.taskapi.entity.TaskStatus;
import com.example.taskapi.repository.TaskRepository;
import com.example.taskapi.specification.TaskSpecifications;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskApiApplicationTests {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void specificationSearchWorks() {
        Specification<Task> spec = Specification.where(TaskSpecifications.hasStatus(TaskStatus.TODO))
                .and(TaskSpecifications.hasPriority(TaskPriority.URGENT));

        List<Task> tasks = taskRepository.findAll(spec);
        assertFalse(tasks.isEmpty());
        tasks.forEach(task -> {
            assertEquals(TaskStatus.TODO, task.getStatus());
            assertEquals(TaskPriority.URGENT, task.getPriority());
        });
    }

    @Test
    void jpqlQueryWorks() {
        List<Task> tasks = taskRepository.findByStatusOrderByPriority(TaskStatus.TODO);
        assertFalse(tasks.isEmpty());
    }

    @Test
    void keywordSearchWorks() {
        List<Task> tasks = taskRepository.searchByKeyword("documentation");
        assertFalse(tasks.isEmpty());
    }

    @Test
    void countByStatusWorks() {
        List<Object[]> counts = taskRepository.countByStatus();
        assertFalse(counts.isEmpty());
    }
}
