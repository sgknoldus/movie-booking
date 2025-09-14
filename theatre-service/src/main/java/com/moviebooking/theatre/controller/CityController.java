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

import com.moviebooking.theatre.model.CityRequest;
import com.moviebooking.theatre.model.CityResponse;
import com.moviebooking.theatre.service.CityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/cities")
@RequiredArgsConstructor
@Tag(name = "City Management", description = "APIs for managing cities")
public class CityController {
    private CityService cityService;
    
    @PostMapping
    @Operation(summary = "Create a new city", description = "Creates a new city in the system")
    public ResponseEntity<CityResponse> createCity(@Valid @RequestBody CityRequest request) {
        CityResponse response = cityService.createCity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get city by ID", description = "Retrieves a city by its ID")
    public ResponseEntity<CityResponse> getCityById(@PathVariable Long id) {
        CityResponse response = cityService.getCityById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all cities", description = "Retrieves all cities")
    public ResponseEntity<List<CityResponse>> getAllCities() {
        List<CityResponse> response = cityService.getAllCities();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search cities by name", description = "Searches cities by name")
    public ResponseEntity<List<CityResponse>> searchCitiesByName(@RequestParam String name) {
        List<CityResponse> response = cityService.searchCitiesByName(name);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update city", description = "Updates an existing city")
    public ResponseEntity<CityResponse> updateCity(@PathVariable Long id, 
                                                  @Valid @RequestBody CityRequest request) {
        CityResponse response = cityService.updateCity(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete city", description = "Deletes a city by its ID")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
        return ResponseEntity.noContent().build();
    }
}