package com.moviebooking.search.controller;

import com.moviebooking.search.model.CityDocument;
import com.moviebooking.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search/cities")
@RequiredArgsConstructor
@Tag(name = "City Search", description = "APIs for searching cities")
public class CitySearchController {
    private final SearchService searchService;
    
    @GetMapping
    @Operation(summary = "Search cities", description = "Search cities by name")
    public ResponseEntity<List<CityDocument>> searchCities(@RequestParam(required = false) String query) {
        List<CityDocument> results = searchService.searchCities(query);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/by-state")
    @Operation(summary = "Search cities by state", description = "Find cities in a specific state")
    public ResponseEntity<List<CityDocument>> searchCitiesByState(@RequestParam String state) {
        List<CityDocument> results = searchService.searchCitiesByState(state);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/by-country")
    @Operation(summary = "Search cities by country", description = "Find cities in a specific country")
    public ResponseEntity<List<CityDocument>> searchCitiesByCountry(@RequestParam String country) {
        List<CityDocument> results = searchService.searchCitiesByCountry(country);
        return ResponseEntity.ok(results);
    }
    
    @PostMapping("/admin/recreate-indices")
    @Operation(summary = "Recreate Elasticsearch indices", description = "Delete and recreate all search indices with correct mappings")
    public ResponseEntity<String> recreateIndices() {
        try {
            searchService.recreateIndices();
            return ResponseEntity.ok("Successfully recreated all Elasticsearch indices");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to recreate indices: " + e.getMessage());
        }
    }
}