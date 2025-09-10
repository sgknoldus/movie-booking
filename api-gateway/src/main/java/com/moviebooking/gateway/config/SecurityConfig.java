package com.moviebooking.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                // Allow Swagger and API docs for all services
                .pathMatchers(
                    "/*/swagger-ui.html",
                    "/*/swagger-ui/**", 
                    "/*/api-docs/**",
                    "/*/webjars/**",
                    // Public authentication endpoints
                    "/api/auth/**",
                    // Health and management endpoints
                    "/actuator/**"
                ).permitAll()
                // Require authentication for all other requests
                .anyExchange().authenticated()
            )
            .build();
    }
}