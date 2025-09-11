package com.moviebooking.theatre.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreenResponse {
    private Long id;
    private String name;
    private Integer totalSeats;
    private Screen.ScreenType screenType;
    private TheatreResponse theatre;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}