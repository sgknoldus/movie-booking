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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/theatres")
@RequiredArgsConstructor
@Tag(
    name = "Theatre Management",
    description = "Comprehensive theatre management system. Handles theatre operations including creation, updates, location-based searches, and capacity management."
)
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
    @Operation(
        summary = "List theatres with optional filters",
        description = "Retrieves theatres based on optional filter criteria. Supports filtering by city, name, or combination. Returns all theatres if no filters are provided."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Theatres retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TheatreResponse.class),
                examples = @ExampleObject(
                    name = "Theatre List Example",
                    value = "[{\"id\":1,\"name\":\"PVR Cinemas Phoenix\",\"address\":\"123 Mall Street\",\"cityId\":1,\"cityName\":\"Mumbai\",\"totalScreens\":8,\"latitude\":19.0760,\"longitude\":72.8777,\"amenities\":[\"PARKING\",\"FOOD_COURT\",\"3D\"],\"isActive\":true}]"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid filter parameters")
    })
    public ResponseEntity<List<TheatreResponse>> getTheatres(
            @Parameter(
                description = "Filter theatres by city ID",
                example = "1"
            )
            @RequestParam(required = false) Long cityId,
            @Parameter(
                description = "Filter theatres by name (partial match supported)",
                example = "PVR"
            )
            @RequestParam(required = false) String name) {

        if (cityId != null && name != null) {
            List<TheatreResponse> response = theatreService.searchTheatresByCityAndName(cityId, name);
            return ResponseEntity.ok(response);
        } else if (cityId != null) {
            List<TheatreResponse> response = theatreService.getTheatresByCity(cityId);
            return ResponseEntity.ok(response);
        } else if (name != null) {
            List<TheatreResponse> response = theatreService.searchTheatresByName(name);
            return ResponseEntity.ok(response);
        } else {
            List<TheatreResponse> response = theatreService.getAllTheatres();
            return ResponseEntity.ok(response);
        }
    }
    
    
    @GetMapping("/nearby")
    @Operation(
        summary = "Find nearby theatres",
        description = "Locates theatres within a specified radius from given coordinates. Useful for location-based theatre discovery and recommendations."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Nearby theatres found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TheatreResponse.class),
                examples = @ExampleObject(
                    name = "Nearby Theatres Example",
                    value = "[{\"id\":1,\"name\":\"PVR Cinemas Phoenix\",\"distance\":2.5,\"latitude\":19.0760,\"longitude\":72.8777}]"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid coordinates or radius")
    })
    public ResponseEntity<List<TheatreResponse>> getTheatresNearLocation(
            @Parameter(
                description = "Latitude coordinate for location search",
                example = "19.0760",
                required = true
            )
            @RequestParam Double latitude,
            @Parameter(
                description = "Longitude coordinate for location search",
                example = "72.8777",
                required = true
            )
            @RequestParam Double longitude,
            @Parameter(
                description = "Search radius in kilometers",
                example = "10.0"
            )
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