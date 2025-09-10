package com.moviebooking.theatre.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ShowRequest {
    @NotNull(message = "Movie ID is required")
    private UUID movieId;
    
    @NotNull(message = "Screen ID is required")
    private UUID screenId;
    
    @NotNull(message = "Show time is required")
    private LocalDateTime showTime;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    @NotNull(message = "Available seats are required")
    private List<String> availableSeats;
}