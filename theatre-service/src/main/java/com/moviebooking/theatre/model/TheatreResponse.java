package com.moviebooking.theatre.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheatreResponse {
    private Long id;
    private String name;
    private String address;
    private String phoneNumber;
    private String email;
    private Double latitude;
    private Double longitude;
    private CityResponse city;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}