package com.moviebooking.theatre.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowResponse {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private LocalDateTime showDateTime;
    private LocalDateTime endDateTime;
    private BigDecimal price;
    private Integer availableSeats;
    private Show.ShowStatus status;
    private ScreenResponse screen;
    private TheatreResponse theatre;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}