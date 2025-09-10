package com.moviebooking.theatre.presentation.controller;

import com.moviebooking.theatre.application.service.TheatreService;
import com.moviebooking.theatre.application.dto.ShowRequest;
import com.moviebooking.theatre.application.dto.ShowResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
@Tag(name = "Show Management", description = "APIs for managing shows")
public class ShowController {

    private final TheatreService theatreService;

    @GetMapping("/{showId}")
    @Operation(summary = "Get show details by ID")
    public ResponseEntity<TheatreService.ShowResponse> getShow(
            @Parameter(description = "Show ID") 
            @PathVariable UUID showId) {
        
        return ResponseEntity.ok(theatreService.getShow(showId));
    }

    @PostMapping
    @Operation(summary = "Create a new show")
    public ResponseEntity<ShowResponse> createShow(@Valid @RequestBody ShowRequest request) {
        ShowResponse response = theatreService.createShow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}