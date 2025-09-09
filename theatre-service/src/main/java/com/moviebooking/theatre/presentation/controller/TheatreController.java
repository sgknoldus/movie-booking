package com.moviebooking.theatre.presentation.controller;

import com.moviebooking.theatre.application.dto.TheatreSearchResponse;
import com.moviebooking.theatre.application.service.TheatreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/theatres")
@RequiredArgsConstructor
@Tag(name = "Theatre Management", description = "APIs for managing theatres and shows")
public class TheatreController {

    private final TheatreService theatreService;

    @GetMapping("/search")
    @Operation(summary = "Search theatres by movie, city and date")
    public ResponseEntity<List<TheatreSearchResponse>> searchTheatres(
            @Parameter(description = "Movie ID") 
            @RequestParam UUID movieId,
            
            @Parameter(description = "City name") 
            @RequestParam String city,
            
            @Parameter(description = "Show date (yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        return ResponseEntity.ok(theatreService.searchTheatres(movieId, city, date));
    }
}
