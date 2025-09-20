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
    @Operation(summary = "Search cities with filters", description = "Search cities with optional filters")
    public ResponseEntity<List<CityDocument>> searchCities(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String country) {

        if (state != null) {
            List<CityDocument> results = searchService.searchCitiesByState(state);
            return ResponseEntity.ok(results);
        }

        if (country != null) {
            List<CityDocument> results = searchService.searchCitiesByCountry(country);
            return ResponseEntity.ok(results);
        }

        List<CityDocument> results = searchService.searchCities(query);
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