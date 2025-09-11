package com.moviebooking.theatre.controller;

import com.moviebooking.theatre.model.Screen;
import com.moviebooking.theatre.model.ScreenRequest;
import com.moviebooking.theatre.model.ScreenResponse;
import com.moviebooking.theatre.service.ScreenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/screens")
@RequiredArgsConstructor
@Tag(name = "Screen Management", description = "APIs for managing screens")
public class ScreenController {
    private final ScreenService screenService;
    
    @PostMapping
    @Operation(summary = "Create a new screen", description = "Creates a new screen in the system")
    public ResponseEntity<ScreenResponse> createScreen(@Valid @RequestBody ScreenRequest request) {
        ScreenResponse response = screenService.createScreen(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get screen by ID", description = "Retrieves a screen by its ID")
    public ResponseEntity<ScreenResponse> getScreenById(@PathVariable Long id) {
        ScreenResponse response = screenService.getScreenById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all screens", description = "Retrieves all screens")
    public ResponseEntity<List<ScreenResponse>> getAllScreens() {
        List<ScreenResponse> response = screenService.getAllScreens();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/by-theatre/{theatreId}")
    @Operation(summary = "Get screens by theatre", description = "Retrieves all screens in a specific theatre")
    public ResponseEntity<List<ScreenResponse>> getScreensByTheatre(@PathVariable Long theatreId) {
        List<ScreenResponse> response = screenService.getScreensByTheatre(theatreId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/by-theatre-type")
    @Operation(summary = "Get screens by theatre and type", description = "Retrieves screens by theatre and screen type")
    public ResponseEntity<List<ScreenResponse>> getScreensByTheatreAndType(
            @RequestParam Long theatreId,
            @RequestParam Screen.ScreenType screenType) {
        List<ScreenResponse> response = screenService.getScreensByTheatreAndType(theatreId, screenType);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update screen", description = "Updates an existing screen")
    public ResponseEntity<ScreenResponse> updateScreen(@PathVariable Long id, 
                                                      @Valid @RequestBody ScreenRequest request) {
        ScreenResponse response = screenService.updateScreen(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete screen", description = "Deletes a screen by its ID")
    public ResponseEntity<Void> deleteScreen(@PathVariable Long id) {
        screenService.deleteScreen(id);
        return ResponseEntity.noContent().build();
    }
}