package com.moviebooking.theatre.controller;

import com.moviebooking.theatre.model.ShowRequest;
import com.moviebooking.theatre.model.ShowResponse;
import com.moviebooking.theatre.service.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shows")
@RequiredArgsConstructor
@Tag(name = "Show Management", description = "APIs for managing shows")
public class ShowController {
    private final ShowService showService;
    
    @PostMapping
    @Operation(summary = "Create a new show", description = "Creates a new show in the system")
    public ResponseEntity<ShowResponse> createShow(@Valid @RequestBody ShowRequest request) {
        ShowResponse response = showService.createShow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get show by ID", description = "Retrieves a show by its ID")
    public ResponseEntity<ShowResponse> getShowById(@PathVariable Long id) {
        ShowResponse response = showService.getShowById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get shows with filters", description = "Retrieves shows with optional filters")
    public ResponseEntity<List<ShowResponse>> getShows(
            @RequestParam(required = false) Long theatreId,
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        // Complex filtering for theatre and date range
        if (theatreId != null && startTime != null && endTime != null) {
            List<ShowResponse> response = showService.getShowsByTheatreAndDateRange(theatreId, startTime, endTime);
            return ResponseEntity.ok(response);
        }

        // Filter by movie and city with optional date
        if (movieId != null && cityId != null) {
            LocalDateTime searchFromDateTime = fromDateTime != null ? fromDateTime : LocalDateTime.now();
            List<ShowResponse> response = showService.getShowsByMovieAndCity(movieId, cityId, searchFromDateTime);
            return ResponseEntity.ok(response);
        }

        // Filter by theatre
        if (theatreId != null) {
            List<ShowResponse> response = showService.getShowsByTheatre(theatreId);
            return ResponseEntity.ok(response);
        }

        // Filter by movie
        if (movieId != null) {
            List<ShowResponse> response = showService.getShowsByMovie(movieId);
            return ResponseEntity.ok(response);
        }

        // Default: get all shows
        List<ShowResponse> response = showService.getAllShows();
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update show", description = "Updates an existing show")
    public ResponseEntity<ShowResponse> updateShow(@PathVariable Long id, 
                                                  @Valid @RequestBody ShowRequest request) {
        ShowResponse response = showService.updateShow(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete show", description = "Deletes a show by its ID")
    public ResponseEntity<Void> deleteShow(@PathVariable Long id) {
        showService.deleteShow(id);
        return ResponseEntity.noContent().build();
    }
}