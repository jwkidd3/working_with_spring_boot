package com.example.ldapdemo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    @GetMapping("/dashboard")
    public Map<String, Object> getManagerDashboard(Authentication authentication) {
        return Map.of(
            "user", authentication.getName(),
            "dashboard", "Manager Dashboard",
            "features", new String[]{
                "View team reports",
                "Approve requests",
                "Manage team members",
                "View analytics"
            }
        );
    }

    @GetMapping("/reports")
    public Map<String, Object> getReports() {
        return Map.of(
            "reports", new String[]{
                "Q1 Sales Report",
                "Team Performance",
                "Budget Analysis"
            },
            "accessLevel", "MANAGER"
        );
    }

    @GetMapping("/team")
    @PreAuthorize("hasRole('MANAGER')")
    public Map<String, Object> getTeamInfo() {
        return Map.of(
            "teamSize", 5,
            "department", "Engineering",
            "members", new String[]{
                "john.doe",
                "alice.wong",
                "bob.johnson"
            }
        );
    }
}
