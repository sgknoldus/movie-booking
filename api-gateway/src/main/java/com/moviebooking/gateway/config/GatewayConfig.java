package com.moviebooking.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Theatre Service Routes
                .route("theatre-service", r -> r
                        .path("/api/theatres/**")
                        .uri("lb://theatre-service"))
                
                // Movie Service Routes
                .route("movie-service", r -> r
                        .path("/api/movies/**")
                        .uri("lb://movie-service"))
                
                // Booking Service Routes
                .route("booking-service", r -> r
                        .path("/api/bookings/**")
                        .uri("lb://booking-service"))
                
                // User Service Routes - Public endpoints
                .route("user-service-public", r -> r
                        .path("/api/auth/**")
                        .uri("lb://user-service"))
                
                // User Service Routes - Protected endpoints
                .route("user-service-protected", r -> r
                        .path("/api/users/**")
                        .uri("lb://user-service"))
                
                // Payment Service Routes
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .uri("lb://payment-service"))
                .build();
    }
}
