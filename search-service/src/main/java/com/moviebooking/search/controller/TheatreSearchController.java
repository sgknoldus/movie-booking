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

    @GetMapping("/admin/count")
    @Operation(summary = "Count theatres", description = "Get total count of indexed theatres")
    public ResponseEntity<Long> countTheatres() {
        long count = searchService.countTheatres();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/admin/health")
    @Operation(summary = "Health check", description = "Check Elasticsearch health")
    public ResponseEntity<Boolean> healthCheck() {
        boolean healthy = searchService.isElasticsearchHealthy();
        return ResponseEntity.ok(healthy);
    }

    @GetMapping("/admin/debug")
    @Operation(summary = "Debug info", description = "Get debug information about theatre indices")
    public ResponseEntity<String> debugInfo() {
        StringBuilder debug = new StringBuilder();
        debug.append("=== Theatre Search Debug Information ===\n");
        debug.append("Elasticsearch Health: ").append(searchService.isElasticsearchHealthy()).append("\n");
        debug.append("Total Theatres Count: ").append(searchService.countTheatres()).append("\n");
        debug.append("Total Cities Count: ").append(searchService.countCities()).append("\n");
        debug.append("Total Shows Count: ").append(searchService.countShows()).append("\n");

        // Get first 3 theatres for debugging
        List<TheatreDocument> sampleTheatres = searchService.searchTheatres(null);
        debug.append("Sample Theatres (first 3):\n");
        sampleTheatres.stream().limit(3).forEach(theatre ->
            debug.append("  - ID: ").append(theatre.getId())
                 .append(", Name: ").append(theatre.getName())
                 .append(", City: ").append(theatre.getCityName())
                 .append("\n")
        );

        return ResponseEntity.ok(debug.toString());
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