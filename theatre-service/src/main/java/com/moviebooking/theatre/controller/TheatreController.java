package com.moviebooking.theatre.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moviebooking.theatre.model.TheatreRequest;
import com.moviebooking.theatre.model.TheatreResponse;
import com.moviebooking.theatre.service.TheatreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/theatres")
@RequiredArgsConstructor
@Tag(name = "Theatre Management", description = "APIs for managing theatres")
@SecurityRequirement(name = "Bearer Authentication")
public class TheatreController {
    private final TheatreService theatreService;
    
    @PostMapping
    @Operation(summary = "Create a new theatre", description = "Creates a new theatre in the system")
    public ResponseEntity<TheatreResponse> createTheatre(@Valid @RequestBody TheatreRequest request) {
        TheatreResponse response = theatreService.createTheatre(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get theatre by ID", description = "Retrieves a theatre by its ID")
    public ResponseEntity<TheatreResponse> getTheatreById(@PathVariable Long id) {
        TheatreResponse response = theatreService.getTheatreById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all theatres", description = "Retrieves all theatres")
    public ResponseEntity<List<TheatreResponse>> getAllTheatres() {
        List<TheatreResponse> response = theatreService.getAllTheatres();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/by-city/{cityId}")
    @Operation(summary = "Get theatres by city", description = "Retrieves all theatres in a specific city")
    public ResponseEntity<List<TheatreResponse>> getTheatresByCity(@PathVariable Long cityId) {
        List<TheatreResponse> response = theatreService.getTheatresByCity(cityId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search theatres by name", description = "Searches theatres by name")
    public ResponseEntity<List<TheatreResponse>> searchTheatresByName(@RequestParam String name) {
        List<TheatreResponse> response = theatreService.searchTheatresByName(name);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search/by-city")
    @Operation(summary = "Search theatres by city and name", description = "Searches theatres by city and name")
    public ResponseEntity<List<TheatreResponse>> searchTheatresByCityAndName(
            @RequestParam Long cityId, 
            @RequestParam String name) {
        List<TheatreResponse> response = theatreService.searchTheatresByCityAndName(cityId, name);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/nearby")
    @Operation(summary = "Get nearby theatres", description = "Retrieves theatres within specified radius")
    public ResponseEntity<List<TheatreResponse>> getTheatresNearLocation(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radiusKm) {
        List<TheatreResponse> response = theatreService.getTheatresNearLocation(latitude, longitude, radiusKm);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update theatre", description = "Updates an existing theatre")
    public ResponseEntity<TheatreResponse> updateTheatre(@PathVariable Long id, 
                                                        @Valid @RequestBody TheatreRequest request) {
        TheatreResponse response = theatreService.updateTheatre(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete theatre", description = "Deletes a theatre by its ID")
    public ResponseEntity<Void> deleteTheatre(@PathVariable Long id) {
        theatreService.deleteTheatre(id);
        return ResponseEntity.noContent().build();
    }
}