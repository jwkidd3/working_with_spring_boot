package com.example.taskapi.config;

import com.example.taskapi.entity.Role;
import com.example.taskapi.entity.User;
import com.example.taskapi.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Create regular user with ROLE_USER
            if (!userRepository.existsByUsername("user")) {
                User user = new User();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("password"));
                user.setEmail("user@example.com");
                user.setRoles(Set.of(Role.ROLE_USER));
                user.setEnabled(true);
                userRepository.save(user);
                System.out.println("Created user: user/password with ROLE_USER");
            }

            // Create admin user with ROLE_USER and ROLE_ADMIN
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@example.com");
                admin.setRoles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN));
                admin.setEnabled(true);
                userRepository.save(admin);
                System.out.println("Created user: admin/admin123 with ROLE_USER and ROLE_ADMIN");
            }
        };
    }
}
