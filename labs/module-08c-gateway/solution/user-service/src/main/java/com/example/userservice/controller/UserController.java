package com.example.userservice.controller;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final Map<Long, Map<String, Object>> users = new HashMap<>();
    private long nextId = 1;

    public UserController() {
        addUser("John Doe", "john@example.com");
        addUser("Jane Smith", "jane@example.com");
    }

    private void addUser(String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", nextId);
        user.put("name", name);
        user.put("email", email);
        users.put(nextId++, user);
    }

    @GetMapping
    public List<Map<String, Object>> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUser(@PathVariable Long id) {
        Map<String, Object> user = users.get(id);
        if (user == null) {
            throw new RuntimeException("User not found: " + id);
        }
        return user;
    }

    @PostMapping
    public Map<String, Object> createUser(@RequestBody Map<String, String> request) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", nextId);
        user.put("name", request.get("name"));
        user.put("email", request.get("email"));
        users.put(nextId++, user);
        return user;
    }
}
