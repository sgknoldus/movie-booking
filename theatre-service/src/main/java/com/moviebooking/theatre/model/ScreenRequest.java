package com.moviebooking.theatre.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreenRequest {
    @NotBlank(message = "Screen name is required")
    private String name;
    
    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Total seats must be at least 1")
    private Integer totalSeats;
    
    private Screen.ScreenType screenType;
    
    @NotNull(message = "Theatre ID is required")
    private Long theatreId;
}