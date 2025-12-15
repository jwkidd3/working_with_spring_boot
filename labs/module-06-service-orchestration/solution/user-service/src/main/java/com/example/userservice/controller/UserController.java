package com.example.userservice.controller;

import com.example.userservice.model.User;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final Map<Long, User> users = new HashMap<>();

    @PostConstruct
    public void initializeUsers() {
        // Initialize with sample users
        users.put(1L, new User(1L, "John Doe", "john.doe@example.com"));
        users.put(2L, new User(2L, "Jane Smith", "jane.smith@example.com"));
        users.put(3L, new User(3L, "Bob Johnson", "bob.johnson@example.com"));
        users.put(4L, new User(4L, "Alice Williams", "alice.williams@example.com"));
        users.put(5L, new User(5L, "Charlie Brown", "charlie.brown@example.com"));

        logger.info("Initialized {} sample users", users.size());
    }

    @GetMapping
    public ResponseEntity<Collection<User>> getAllUsers() {
        logger.info("GET /api/users - Fetching all users");
        return ResponseEntity.ok(users.values());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        logger.info("GET /api/users/{} - Fetching user by ID", id);

        User user = users.get(id);
        if (user != null) {
            logger.info("User found: {}", user);
            return ResponseEntity.ok(user);
        } else {
            logger.warn("User not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        logger.info("POST /api/users - Creating new user: {}", request.name());

        Long newId = users.keySet().stream()
                .max(Long::compareTo)
                .orElse(0L) + 1;

        User newUser = new User(newId, request.name(), request.email());
        users.put(newId, newUser);

        logger.info("Created user: {}", newUser);
        return ResponseEntity.ok(newUser);
    }
}

record CreateUserRequest(String name, String email) {
}
