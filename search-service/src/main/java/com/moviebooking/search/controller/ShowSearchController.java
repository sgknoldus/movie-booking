package com.moviebooking.search.controller;

import com.moviebooking.search.model.ShowDocument;
import com.moviebooking.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(
    name = "Show Search",
    description = "Advanced show discovery and search capabilities. Provides comprehensive filtering options including location, time, movie, and theatre-based searches using Elasticsearch."
)
public class ShowSearchController {
    private final SearchService searchService;
    
    @GetMapping
    @Operation(
        summary = "Advanced show search with comprehensive filters",
        description = "Performs intelligent show searches using Elasticsearch with support for multiple filter combinations. Supports text search, location-based filtering, date ranges, and complex multi-criteria searches."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Shows found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ShowDocument.class),
                examples = @ExampleObject(
                    name = "Show Search Results",
                    value = "[{\"id\":1,\"movieId\":101,\"movieTitle\":\"Avengers: Endgame\",\"theatreId\":201,\"theatreName\":\"PVR Phoenix\",\"cityId\":1,\"cityName\":\"Mumbai\",\"showDateTime\":\"2024-01-15T19:30:00Z\",\"screenName\":\"Screen 1\",\"language\":\"English\",\"format\":\"IMAX\",\"price\":350.00,\"availableSeats\":45}]"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid search parameters or date formats")
    })
    public ResponseEntity<List<ShowDocument>> searchShows(
            @Parameter(
                description = "Free text search query for movie titles, theatre names, or general search",
                example = "avengers"
            )
            @RequestParam(required = false) String query,
            @Parameter(
                description = "Filter shows by specific movie ID",
                example = "101"
            )
            @RequestParam(required = false) Long movieId,
            @Parameter(
                description = "Filter shows by specific theatre ID",
                example = "201"
            )
            @RequestParam(required = false) Long theatreId,
            @Parameter(
                description = "Filter shows by city ID",
                example = "1"
            )
            @RequestParam(required = false) Long cityId,
            @Parameter(
                description = "Filter shows by movie title (partial match supported)",
                example = "Avengers"
            )
            @RequestParam(required = false) String movieTitle,
            @Parameter(
                description = "Filter shows by city name",
                example = "Mumbai"
            )
            @RequestParam(required = false) String cityName,
            @Parameter(
                description = "Filter shows by theatre name (partial match supported)",
                example = "PVR"
            )
            @RequestParam(required = false) String theatreName,
            @Parameter(
                description = "Show start time filter - shows from this date/time onwards (ISO 8601 format)",
                example = "2024-01-15T18:00:00"
            )
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDateTime,
            @Parameter(
                description = "Show end time filter - shows until this date/time (ISO 8601 format)",
                example = "2024-01-15T23:59:59"
            )
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDateTime,
            @Parameter(
                description = "Alternative start date for date range search (ISO 8601 format)",
                example = "2024-01-15T00:00:00"
            )
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(
                description = "Alternative end date for date range search (ISO 8601 format)",
                example = "2024-01-16T23:59:59"
            )
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // Advanced filtering with multiple parameters
        if (movieTitle != null || cityName != null || theatreName != null || fromDateTime != null || toDateTime != null) {
            List<ShowDocument> results = searchService.searchShowsWithFilters(
                    movieTitle, cityName, theatreName, fromDateTime, toDateTime);
            return ResponseEntity.ok(results);
        }

        // Date range search
        if (startDate != null && endDate != null) {
            List<ShowDocument> results = searchService.searchShowsByDateRange(startDate, endDate);
            return ResponseEntity.ok(results);
        }

        // Movie and city combination
        if (movieId != null && cityId != null) {
            List<ShowDocument> results;
            if (fromDateTime != null) {
                results = searchService.searchShowsByMovieAndCityAfterDateTime(movieId, cityId, fromDateTime);
            } else {
                results = searchService.searchShowsByMovieAndCity(movieId, cityId);
            }
            return ResponseEntity.ok(results);
        }

        // Theatre name search
        if (theatreName != null) {
            List<ShowDocument> results = searchService.searchShowsByTheatreName(theatreName);
            return ResponseEntity.ok(results);
        }

        // City name search
        if (cityName != null) {
            List<ShowDocument> results = searchService.searchShowsByCityName(cityName);
            return ResponseEntity.ok(results);
        }

        // Movie ID search
        if (movieId != null) {
            List<ShowDocument> results = searchService.searchShowsByMovie(movieId);
            return ResponseEntity.ok(results);
        }

        // Theatre ID search
        if (theatreId != null) {
            List<ShowDocument> results = searchService.searchShowsByTheatre(theatreId);
            return ResponseEntity.ok(results);
        }

        // City ID search
        if (cityId != null) {
            List<ShowDocument> results = searchService.searchShowsByCity(cityId);
            return ResponseEntity.ok(results);
        }

        // Default: search by query or all
        List<ShowDocument> results = searchService.searchShows(query);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/admin/count")
    @Operation(summary = "Count shows", description = "Get total count of indexed shows")
    public ResponseEntity<Long> countShows() {
        long count = searchService.countShows();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/admin/debug")
    @Operation(
        summary = "Debug show information",
        description = "Get comprehensive debug information about show indices. Optionally filter by theatre ID for detailed theatre-specific show analysis."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Debug information retrieved successfully",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(
                    name = "Debug Info Example",
                    value = "=== Shows Debug Information ===\nElasticsearch Health: true\nTotal Shows Count: 150\nTotal Theatres Count: 25\nTotal Cities Count: 5\nSample Shows (first 3):\n  - ID: 1, Movie: Avengers, Theatre: PVR Phoenix\n"
                )
            )
        )
    })
    public ResponseEntity<String> debugInfo(
            @Parameter(
                description = "Optional theatre ID for theatre-specific debug information",
                example = "201"
            )
            @RequestParam(required = false) Long theatreId) {

        StringBuilder debug = new StringBuilder();
        debug.append("=== Shows Debug Information ===\n");
        debug.append("Elasticsearch Health: ").append(searchService.isElasticsearchHealthy()).append("\n");
        debug.append("Total Shows Count: ").append(searchService.countShows()).append("\n");
        debug.append("Total Theatres Count: ").append(searchService.countTheatres()).append("\n");
        debug.append("Total Cities Count: ").append(searchService.countCities()).append("\n");

        if (theatreId != null) {
            debug.append("\n--- Theatre-Specific Debug (Theatre ID: ").append(theatreId).append(") ---\n");
            List<ShowDocument> shows = searchService.searchShowsByTheatre(theatreId);
            debug.append("Shows found for Theatre ID ").append(theatreId).append(": ").append(shows.size()).append("\n");

            if (shows.isEmpty()) {
                debug.append("No shows found. Checking all shows for comparison:\n");
                List<ShowDocument> allShows = searchService.searchShows(null);
                debug.append("Total shows in index: ").append(allShows.size()).append("\n");

                allShows.stream().limit(5).forEach(show ->
                    debug.append("  - Show ID: ").append(show.getId())
                         .append(", Theatre ID: ").append(show.getTheatreId())
                         .append(", Theatre Name: ").append(show.getTheatreName())
                         .append(", Movie: ").append(show.getMovieTitle())
                         .append("\n")
                );
            } else {
                shows.forEach(show ->
                    debug.append("  - Show ID: ").append(show.getId())
                         .append(", Movie: ").append(show.getMovieTitle())
                         .append(", DateTime: ").append(show.getShowDateTime())
                         .append("\n")
                );
            }
        } else {
            debug.append("\n--- General Show Information ---\n");
            List<ShowDocument> sampleShows = searchService.searchShows(null);
            debug.append("Sample Shows (first 3):\n");
            sampleShows.stream().limit(3).forEach(show ->
                debug.append("  - ID: ").append(show.getId())
                     .append(", Movie: ").append(show.getMovieTitle())
                     .append(", Theatre: ").append(show.getTheatreName())
                     .append(" (ID: ").append(show.getTheatreId()).append(")")
                     .append(", City: ").append(show.getCityName())
                     .append("\n")
            );
        }

        return ResponseEntity.ok(debug.toString());
    }
}