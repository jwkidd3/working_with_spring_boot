package com.example.ldapdemo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/profile")
    public Map<String, Object> getProfile(Authentication authentication) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", authentication.getName());
        profile.put("roles", authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
        profile.put("authenticated", authentication.isAuthenticated());
        profile.put("message", "Welcome! You have USER access.");
        return profile;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard(Authentication authentication) {
        return Map.of(
            "user", authentication.getName(),
            "dashboard", "User Dashboard",
            "features", new String[]{
                "View profile",
                "Update settings",
                "View reports"
            }
        );
    }
}
