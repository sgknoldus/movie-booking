package com.moviebooking.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @GetMapping("/theatre")
    @PostMapping("/theatre")
    public Mono<ResponseEntity<Map<String, Object>>> theatreFallback() {
        log.warn("Theatre service fallback triggered at {}", LocalDateTime.now());

        Map<String, Object> fallbackResponse = Map.of(
                "error", "Theatre Service Unavailable",
                "message", "The theatre service is currently experiencing issues. Please try again later.",
                "timestamp", LocalDateTime.now(),
                "service", "theatre-service",
                "fallback", true,
                "suggestion", "Try browsing available movies or check back in a few minutes"
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackResponse));
    }

    @GetMapping("/booking")
    @PostMapping("/booking")
    public Mono<ResponseEntity<Map<String, Object>>> bookingFallback() {
        log.warn("Booking service fallback triggered at {}", LocalDateTime.now());

        Map<String, Object> fallbackResponse = Map.of(
                "error", "Booking Service Unavailable",
                "message", "The booking service is currently unavailable. Your booking request could not be processed.",
                "timestamp", LocalDateTime.now(),
                "service", "booking-service",
                "fallback", true,
                "suggestion", "Please try booking again in a few minutes. No charges have been made.",
                "supportContact", "support@moviebooking.com"
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackResponse));
    }

    @GetMapping("/payment")
    @PostMapping("/payment")
    public Mono<ResponseEntity<Map<String, Object>>> paymentFallback() {
        log.error("Payment service fallback triggered at {}", LocalDateTime.now());

        Map<String, Object> fallbackResponse = Map.of(
                "error", "Payment Service Unavailable",
                "message", "Payment processing is currently unavailable. No charges have been made.",
                "timestamp", LocalDateTime.now(),
                "service", "payment-service",
                "fallback", true,
                "suggestion", "Please retry your payment in a few minutes or contact support",
                "supportContact", "payments@moviebooking.com",
                "emergencyContact", "+1-800-MOVIE-HELP"
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackResponse));
    }

    @GetMapping("/user")
    @PostMapping("/user")
    public Mono<ResponseEntity<Map<String, Object>>> userFallback() {
        log.warn("User service fallback triggered at {}", LocalDateTime.now());

        Map<String, Object> fallbackResponse = Map.of(
                "error", "User Service Unavailable",
                "message", "User management service is currently unavailable.",
                "timestamp", LocalDateTime.now(),
                "service", "user-service",
                "fallback", true,
                "suggestion", "Please try accessing your account later"
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackResponse));
    }

    @GetMapping("/auth")
    @PostMapping("/auth")
    public Mono<ResponseEntity<Map<String, Object>>> authFallback() {
        log.error("Authentication service fallback triggered at {}", LocalDateTime.now());

        Map<String, Object> fallbackResponse = Map.of(
                "error", "Authentication Service Unavailable",
                "message", "Authentication service is currently unavailable. Please try logging in later.",
                "timestamp", LocalDateTime.now(),
                "service", "user-service",
                "fallback", true,
                "suggestion", "Please try logging in again in a few minutes"
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackResponse));
    }

    @GetMapping("/search")
    @PostMapping("/search")
    public Mono<ResponseEntity<Map<String, Object>>> searchFallback() {
        log.warn("Search service fallback triggered at {}", LocalDateTime.now());

        Map<String, Object> fallbackResponse = Map.of(
                "error", "Search Service Unavailable",
                "message", "Search functionality is currently unavailable.",
                "timestamp", LocalDateTime.now(),
                "service", "search-service",
                "fallback", true,
                "data", Map.of(
                        "movies", "[]",
                        "theatres", "[]",
                        "suggestions", "[]"
                ),
                "suggestion", "Try browsing by category or check back later"
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackResponse));
    }

    @GetMapping("/ticket")
    @PostMapping("/ticket")
    public Mono<ResponseEntity<Map<String, Object>>> ticketFallback() {
        log.warn("Ticket service fallback triggered at {}", LocalDateTime.now());

        Map<String, Object> fallbackResponse = Map.of(
                "error", "Ticket Service Unavailable",
                "message", "Ticket generation service is currently unavailable.",
                "timestamp", LocalDateTime.now(),
                "service", "ticket-service",
                "fallback", true,
                "suggestion", "Your booking is confirmed. Tickets will be available shortly.",
                "note", "Check your email for booking confirmation"
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackResponse));
    }

    @GetMapping("/notification")
    @PostMapping("/notification")
    public Mono<ResponseEntity<Map<String, Object>>> notificationFallback() {
        log.warn("Notification service fallback triggered at {}", LocalDateTime.now());

        Map<String, Object> fallbackResponse = Map.of(
                "error", "Notification Service Unavailable",
                "message", "Notification service is currently unavailable.",
                "timestamp", LocalDateTime.now(),
                "service", "notification-service",
                "fallback", true,
                "suggestion", "Notifications will be sent once service is restored"
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackResponse));
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> fallbackHealth() {
        Map<String, Object> healthResponse = Map.of(
                "status", "DEGRADED",
                "message", "Some services are unavailable, operating in fallback mode",
                "timestamp", LocalDateTime.now(),
                "fallbacksActive", true
        );

        return Mono.just(ResponseEntity.ok(healthResponse));
    }
}