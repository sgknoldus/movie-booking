package com.moviebooking.booking.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BookingRequest {
    @NotNull(message = "Theatre ID is required")
    private UUID theatreId;
    
    @NotNull(message = "Show ID is required")
    private UUID showId;
    
    @NotNull(message = "Movie ID is required")
    private UUID movieId;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotEmpty(message = "At least one seat must be selected")
    private List<String> seats;
}
