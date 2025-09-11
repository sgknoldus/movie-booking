package com.moviebooking.search.controller;

import com.moviebooking.search.model.TheatreDocument;
import com.moviebooking.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search/theatres")
@RequiredArgsConstructor
@Tag(name = "Theatre Search", description = "APIs for searching theatres")
public class TheatreSearchController {
    private final SearchService searchService;
    
    @GetMapping
    @Operation(summary = "Search theatres", description = "Search theatres by name")
    public ResponseEntity<List<TheatreDocument>> searchTheatres(@RequestParam(required = false) String query) {
        List<TheatreDocument> results = searchService.searchTheatres(query);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/by-city/{cityId}")
    @Operation(summary = "Search theatres by city", description = "Find theatres in a specific city")
    public ResponseEntity<List<TheatreDocument>> searchTheatresByCity(@PathVariable Long cityId) {
        List<TheatreDocument> results = searchService.searchTheatresByCity(cityId);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/by-city-name")
    @Operation(summary = "Search theatres by city name", description = "Find theatres by city name")
    public ResponseEntity<List<TheatreDocument>> searchTheatresByCityName(@RequestParam String cityName) {
        List<TheatreDocument> results = searchService.searchTheatresByCityName(cityName);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/by-address")
    @Operation(summary = "Search theatres by address", description = "Find theatres by address")
    public ResponseEntity<List<TheatreDocument>> searchTheatresByAddress(@RequestParam String address) {
        List<TheatreDocument> results = searchService.searchTheatresByAddress(address);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/nearby")
    @Operation(summary = "Search nearby theatres", description = "Find theatres near a location")
    public ResponseEntity<List<TheatreDocument>> searchTheatresNearLocation(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10km") String distance) {
        List<TheatreDocument> results = searchService.searchTheatresNearLocation(latitude, longitude, distance);
        return ResponseEntity.ok(results);
    }
}