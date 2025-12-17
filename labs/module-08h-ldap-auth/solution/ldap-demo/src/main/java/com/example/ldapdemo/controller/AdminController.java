package com.example.ldapdemo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public Map<String, Object> getAdminDashboard(Authentication authentication) {
        return Map.of(
            "user", authentication.getName(),
            "dashboard", "Admin Dashboard",
            "features", new String[]{
                "User management",
                "System configuration",
                "Audit logs",
                "Security settings",
                "Database management"
            }
        );
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getAllUsers() {
        return Map.of(
            "users", new Object[]{
                Map.of("username", "john.doe", "role", "USER", "status", "active"),
                Map.of("username", "jane.smith", "role", "MANAGER", "status", "active"),
                Map.of("username", "admin.user", "role", "ADMIN", "status", "active")
            },
            "totalCount", 3
        );
    }

    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getAuditLog() {
        return Map.of(
            "auditEntries", new Object[]{
                Map.of("action", "LOGIN", "user", "john.doe", "timestamp", "2024-01-15T10:30:00"),
                Map.of("action", "ACCESS", "user", "jane.smith", "timestamp", "2024-01-15T11:00:00"),
                Map.of("action", "CONFIG_CHANGE", "user", "admin.user", "timestamp", "2024-01-15T11:30:00")
            }
        );
    }

    @PostMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> updateConfig(@RequestBody Map<String, String> config) {
        return Map.of(
            "status", "success",
            "message", "Configuration updated",
            "updatedKeys", String.join(", ", config.keySet())
        );
    }
}
