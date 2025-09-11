package com.moviebooking.theatre.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowRequest {
    @NotNull(message = "Movie ID is required")
    private Long movieId;
    
    @NotBlank(message = "Movie title is required")
    private String movieTitle;
    
    @NotNull(message = "Show date time is required")
    private LocalDateTime showDateTime;
    
    @NotNull(message = "End date time is required")
    private LocalDateTime endDateTime;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    @NotNull(message = "Screen ID is required")
    private Long screenId;
    
    @NotNull(message = "Theatre ID is required")
    private Long theatreId;
}