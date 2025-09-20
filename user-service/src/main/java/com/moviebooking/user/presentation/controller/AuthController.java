package com.moviebooking.user.presentation.controller;

import com.moviebooking.user.application.dto.AuthResponse;
import com.moviebooking.user.application.dto.LoginRequest;
import com.moviebooking.user.application.dto.RegisterRequest;
import com.moviebooking.user.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs for user registration and login")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
        summary = "User Registration",
        description = "Register a new user and receive JWT token automatically"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Registration successful",
            content = @Content(
                examples = @ExampleObject(
                    name = "Successful Registration",
                    value = "{\"token\":\"eyJhbGciOiJIUzUxMiJ9...\",\"type\":\"Bearer\",\"email\":\"newuser@example.com\",\"name\":\"Jane Doe\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Validation error or user already exists")
    })
    public ResponseEntity<AuthResponse> register(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Registration details",
            content = @Content(
                examples = @ExampleObject(
                    name = "Registration Example",
                    value = "{\"name\":\"Jane Doe\",\"email\":\"newuser@example.com\",\"password\":\"password123\",\"phone\":\"+1234567890\"}"
                )
            )
        )
        @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(
        summary = "User Login",
        description = "Authenticate a user and receive JWT token for accessing protected endpoints"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                examples = @ExampleObject(
                    name = "Successful Login",
                    value = "{\"token\":\"eyJhbGciOiJIUzUxMiJ9...\",\"type\":\"Bearer\",\"email\":\"user@example.com\",\"name\":\"John Doe\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid email format or missing fields"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Login credentials",
            content = @Content(
                examples = @ExampleObject(
                    name = "Login Example",
                    value = "{\"email\":\"user@example.com\",\"password\":\"password123\"}"
                )
            )
        )
        @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
