package com.moviebooking.user.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Authentication response containing JWT token and user information")
public class AuthResponse {
    @Schema(description = "JWT token for authentication", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjM5NzQwMDAwLCJleHAiOjE2Mzk3NDM2MDB9...")
    private String token;

    @Schema(description = "Token type", example = "Bearer")
    private String type;

    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @Schema(description = "User's full name", example = "John Doe")
    private String name;
}
