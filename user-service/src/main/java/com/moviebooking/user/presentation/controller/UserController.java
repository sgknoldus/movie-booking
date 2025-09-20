package com.moviebooking.user.presentation.controller;

import com.moviebooking.user.application.dto.UpdateProfileRequest;
import com.moviebooking.user.application.dto.UserProfileResponse;
import com.moviebooking.user.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(
    name = "User Management",
    description = "Comprehensive user profile management APIs. Handles user data retrieval, updates, and deletion with proper authentication and authorization."
)
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    @Operation(
        summary = "Retrieve user profile by ID",
        description = "Fetches detailed user profile information including personal details, preferences, and account status. Requires valid authentication token."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User profile retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserProfileResponse.class),
                examples = @ExampleObject(
                    name = "User Profile Example",
                    value = "{\"id\":\"123e4567-e89b-12d3-a456-426614174000\",\"name\":\"John Doe\",\"email\":\"john.doe@example.com\",\"phone\":\"+1234567890\",\"createdAt\":\"2024-01-01T10:00:00Z\",\"isActive\":true}"
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Access denied for this user"),
        @ApiResponse(responseCode = "404", description = "User not found with the provided ID")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @Parameter(
                description = "Unique identifier of the user (UUID format)",
                example = "123e4567-e89b-12d3-a456-426614174000",
                required = true
            )
            @PathVariable UUID userId) {
        UserProfileResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
        summary = "List all users (Admin only)",
        description = "Retrieves a complete list of all registered users in the system. This endpoint is restricted to administrators only and requires admin-level authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Users list retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserProfileResponse.class),
                examples = @ExampleObject(
                    name = "Users List Example",
                    value = "[{\"id\":\"123e4567-e89b-12d3-a456-426614174000\",\"name\":\"John Doe\",\"email\":\"john.doe@example.com\"},{\"id\":\"987fcdeb-51a2-43d1-9c45-123456789abc\",\"name\":\"Jane Smith\",\"email\":\"jane.smith@example.com\"}]"
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin privileges required")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers() {
        List<UserProfileResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}")
    @Operation(
        summary = "Update user profile",
        description = "Updates user profile information including name, email, phone number, and other personal details. Users can only update their own profiles unless they have admin privileges."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User profile updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserProfileResponse.class),
                examples = @ExampleObject(
                    name = "Updated Profile Example",
                    value = "{\"id\":\"123e4567-e89b-12d3-a456-426614174000\",\"name\":\"John Updated\",\"email\":\"john.updated@example.com\",\"phone\":\"+1987654321\",\"updatedAt\":\"2024-01-02T15:30:00Z\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data or validation errors"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Cannot update another user's profile"),
        @ApiResponse(responseCode = "404", description = "User not found with the provided ID"),
        @ApiResponse(responseCode = "409", description = "Conflict - Email already exists for another user")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Parameter(
                description = "Unique identifier of the user to update",
                example = "123e4567-e89b-12d3-a456-426614174000",
                required = true
            )
            @PathVariable UUID userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User profile update request containing the fields to be updated",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UpdateProfileRequest.class),
                    examples = @ExampleObject(
                        name = "Update Profile Request",
                        value = "{\"name\":\"John Updated\",\"email\":\"john.updated@example.com\",\"phone\":\"+1987654321\"}"
                    )
                )
            )
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    @Operation(
        summary = "Delete user account",
        description = "Permanently deletes a user account and all associated data. This action is irreversible. Users can only delete their own accounts unless they have admin privileges."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "User account deleted successfully (No Content)"
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Cannot delete another user's account"),
        @ApiResponse(responseCode = "404", description = "User not found with the provided ID"),
        @ApiResponse(responseCode = "409", description = "Conflict - Cannot delete user with active bookings or pending transactions")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Void> deleteUser(
            @Parameter(
                description = "Unique identifier of the user to delete",
                example = "123e4567-e89b-12d3-a456-426614174000",
                required = true
            )
            @PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}