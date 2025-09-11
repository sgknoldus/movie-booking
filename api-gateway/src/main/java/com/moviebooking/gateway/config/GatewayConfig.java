package com.moviebooking.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                
                // Show Service Routes (Theatre Service)
                .route("show-service", r -> r
                        .path("/api/shows/**")
                        .uri("lb://theatre-service"))
                
                // Notification Service Routes
                .route("notification-service", r -> r
                        .path("/api/v1/notifications/**")
                        .uri("lb://notification-service"))
                
                // Swagger UI Routes for all services
                .route("swagger-movie-service", r -> r
                        .path("/movie-service/swagger-ui.html")
                        .uri("lb://movie-service"))
                .route("swagger-user-service", r -> r
                        .path("/user-service/swagger-ui.html")
                        .uri("lb://user-service"))
                .route("swagger-theatre-service", r -> r
                        .path("/theatre-service/swagger-ui.html")
                        .uri("lb://theatre-service"))
                .route("swagger-booking-service", r -> r
                        .path("/booking-service/swagger-ui.html")
                        .uri("lb://booking-service"))
                .route("swagger-payment-service", r -> r
                        .path("/payment-service/swagger-ui.html")
                        .uri("lb://payment-service"))
                .route("swagger-notification-service", r -> r
                        .path("/notification-service/swagger-ui.html")
                        .uri("lb://notification-service"))
                
                // Swagger UI static resources for all services
                .route("swagger-ui-resources-movie", r -> r
                        .path("/movie-service/swagger-ui/**")
                        .uri("lb://movie-service"))
                .route("swagger-ui-resources-user", r -> r
                        .path("/user-service/swagger-ui/**")
                        .uri("lb://user-service"))
                .route("swagger-ui-resources-theatre", r -> r
                        .path("/theatre-service/swagger-ui/**")
                        .uri("lb://theatre-service"))
                .route("swagger-ui-resources-booking", r -> r
                        .path("/booking-service/swagger-ui/**")
                        .uri("lb://booking-service"))
                .route("swagger-ui-resources-payment", r -> r
                        .path("/payment-service/swagger-ui/**")
                        .uri("lb://payment-service"))
                .route("swagger-ui-resources-notification", r -> r
                        .path("/notification-service/swagger-ui/**")
                        .uri("lb://notification-service"))
                
                // API Documentation endpoints - exact paths
                .route("api-docs-movie-exact", r -> r
                        .path("/movie-service/api-docs")
                        .uri("lb://movie-service"))
                .route("api-docs-user-exact", r -> r
                        .path("/user-service/api-docs")
                        .uri("lb://user-service"))
                .route("api-docs-theatre-exact", r -> r
                        .path("/theatre-service/api-docs")
                        .uri("lb://theatre-service"))
                .route("api-docs-booking-exact", r -> r
                        .path("/booking-service/api-docs")
                        .uri("lb://booking-service"))
                .route("api-docs-payment-exact", r -> r
                        .path("/payment-service/api-docs")
                        .uri("lb://payment-service"))
                .route("api-docs-notification-exact", r -> r
                        .path("/notification-service/api-docs")
                        .uri("lb://notification-service"))
                        
                // API Documentation endpoints - with sub-paths
                .route("api-docs-movie", r -> r
                        .path("/movie-service/api-docs/**")
                        .uri("lb://movie-service"))
                .route("api-docs-user", r -> r
                        .path("/user-service/api-docs/**")
                        .uri("lb://user-service"))
                .route("api-docs-theatre", r -> r
                        .path("/theatre-service/api-docs/**")
                        .uri("lb://theatre-service"))
                .route("api-docs-booking", r -> r
                        .path("/booking-service/api-docs/**")
                        .uri("lb://booking-service"))
                .route("api-docs-payment", r -> r
                        .path("/payment-service/api-docs/**")
                        .uri("lb://payment-service"))
                .route("api-docs-notification", r -> r
                        .path("/notification-service/api-docs/**")
                        .uri("lb://notification-service"))
                
                // Webjars for Swagger dependencies
                .route("webjars-movie", r -> r
                        .path("/movie-service/webjars/**")
                        .uri("lb://movie-service"))
                .route("webjars-user", r -> r
                        .path("/user-service/webjars/**")
                        .uri("lb://user-service"))
                .route("webjars-theatre", r -> r
                        .path("/theatre-service/webjars/**")
                        .uri("lb://theatre-service"))
                .route("webjars-booking", r -> r
                        .path("/booking-service/webjars/**")
                        .uri("lb://booking-service"))
                .route("webjars-payment", r -> r
                        .path("/payment-service/webjars/**")
                        .uri("lb://payment-service"))
                .route("webjars-notification", r -> r
                        .path("/notification-service/webjars/**")
                        .uri("lb://notification-service"))
                .build();
    }
}
