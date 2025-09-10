package com.moviebooking.theatre.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TheatreRequest {
    @NotBlank(message = "Theatre name is required")
    private String name;
    
    @NotBlank(message = "City is required")
    private String city;
    
    private String address;
}