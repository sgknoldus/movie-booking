package com.moviebooking.theatre.application.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ShowResponse {
    private UUID id;
    private UUID movieId;
    private UUID screenId;
    private UUID theatreId;
    private String theatreName;
    private Integer screenNumber;
    private LocalDateTime showTime;
    private BigDecimal price;
    private List<String> availableSeats;
}