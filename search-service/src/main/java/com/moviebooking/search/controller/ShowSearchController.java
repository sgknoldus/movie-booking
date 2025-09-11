package com.moviebooking.search.controller;

import com.moviebooking.search.model.ShowDocument;
import com.moviebooking.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/search/shows")
@RequiredArgsConstructor
@Tag(name = "Show Search", description = "APIs for searching shows")
public class ShowSearchController {
    private final SearchService searchService;
    
    @GetMapping
    @Operation(summary = "Search shows", description = "Search shows by movie title")
    public ResponseEntity<List<ShowDocument>> searchShows(@RequestParam(required = false) String query) {
        List<ShowDocument> results = searchService.searchShows(query);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/by-movie/{movieId}")
    @Operation(summary = "Search shows by movie", description = "Find shows for a specific movie")
    public ResponseEntity<List<ShowDocument>> searchShowsByMovie(@PathVariable Long movieId) {
        List<ShowDocument> results = searchService.searchShowsByMovie(movieId);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/by-theatre/{theatreId}")
    @Operation(summary = "Search shows by theatre", description = "Find shows in a specific theatre")
    public ResponseEntity<List<ShowDocument>> searchShowsByTheatre(@PathVariable Long theatreId) {
        List<ShowDocument> results = searchService.searchShowsByTheatre(theatreId);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/by-city/{cityId}")
    @Operation(summary = "Search shows by city", description = "Find shows in a specific city")
    public ResponseEntity<List<ShowDocument>> searchShowsByCity(@PathVariable Long cityId) {
        List<ShowDocument> results = searchService.searchShowsByCity(cityId);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/by-movie-city")
    @Operation(summary = "Search shows by movie and city", description = "Find shows for a movie in a specific city")
    public ResponseEntity<List<ShowDocument>> searchShowsByMovieAndCity(
            @RequestParam Long movieId,
            @RequestParam Long cityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDateTime) {
        
        List<ShowDocument> results;
        if (fromDateTime != null) {
            results = searchService.searchShowsByMovieAndCityAfterDateTime(movieId, cityId, fromDateTime);
        } else {
            results = searchService.searchShowsByMovieAndCity(movieId, cityId);
        }
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/by-date-range")
    @Operation(summary = "Search shows by date range", description = "Find shows within a date range")
    public ResponseEntity<List<ShowDocument>> searchShowsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<ShowDocument> results = searchService.searchShowsByDateRange(startDate, endDate);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/by-theatre-name")
    @Operation(summary = "Search shows by theatre name", description = "Find shows by theatre name")
    public ResponseEntity<List<ShowDocument>> searchShowsByTheatreName(@RequestParam String theatreName) {
        List<ShowDocument> results = searchService.searchShowsByTheatreName(theatreName);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/by-city-name")
    @Operation(summary = "Search shows by city name", description = "Find shows by city name")
    public ResponseEntity<List<ShowDocument>> searchShowsByCityName(@RequestParam String cityName) {
        List<ShowDocument> results = searchService.searchShowsByCityName(cityName);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/advanced")
    @Operation(summary = "Advanced show search", description = "Search shows with multiple filters")
    public ResponseEntity<List<ShowDocument>> searchShowsWithFilters(
            @RequestParam(required = false) String movieTitle,
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) String theatreName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDateTime) {
        
        List<ShowDocument> results = searchService.searchShowsWithFilters(
                movieTitle, cityName, theatreName, fromDateTime, toDateTime);
        return ResponseEntity.ok(results);
    }
}