package com.example.demo.config;

import com.example.demo.user.Role;
import com.example.demo.user.RoleRepository;
import com.example.demo.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserService userService;

    @Bean
    public CommandLineRunner initializeData(RoleRepository roleRepository) {
        return args -> {
            log.info("Initializing default roles...");
            userService.initializeDefaultRoles();
            log.info("Default roles initialized successfully");

            log.info("Creating initial admin user if not exists...");
            userService.createInitialAdminUser();
            log.info("Admin user ready: admin/admin123");
        };
    }
}
