package com.moviebooking.booking.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "theatre-service")
public interface TheatreServiceClient {
    
    @GetMapping("/api/shows/{showId}")
    ShowResponse getShow(@PathVariable UUID showId);

    record ShowResponse(
        UUID showId,
        UUID theatreId,
        UUID movieId,
        String showTime,
        Double price,
        String availableSeats
    ) {}
}
