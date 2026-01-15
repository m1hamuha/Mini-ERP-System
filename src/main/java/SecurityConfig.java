package com.altenburg.erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Для упрощения API
            .cors(cors -> cors.configurationSource(request -> {
                var config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:3000"));
                config.setAllowedMethods(List.of("GET", "POST", "DELETE", "PUT"));
                config.setAllowCredentials(true);
                config.addAllowedHeader("*");
                return config;
            }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {}); // Простая авторизация для демо
        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        // Пароли: admin123, manager123
        UserDetails admin = User.withDefaultPasswordEncoder()
            .username("admin").password("admin123").roles("ADMIN").build();
        UserDetails manager = User.withDefaultPasswordEncoder()
            .username("manager").password("manager123").roles("MANAGER").build();
        return new InMemoryUserDetailsManager(admin, manager);
    }
}
