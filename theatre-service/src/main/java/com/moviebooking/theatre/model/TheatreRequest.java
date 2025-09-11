package com.moviebooking.theatre.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheatreRequest {
    @NotBlank(message = "Theatre name is required")
    private String name;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    private String phoneNumber;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private Double latitude;
    private Double longitude;
    
    @NotNull(message = "City ID is required")
    private Long cityId;
}