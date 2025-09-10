package com.moviebooking.movie.presentation.controller;

import com.moviebooking.movie.application.dto.MovieRequest;
import com.moviebooking.movie.application.dto.MovieResponse;
import com.moviebooking.movie.application.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Tag(name = "Movie Management", description = "APIs for managing movies")
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    @Operation(summary = "Create a new movie")
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest request) {
        MovieResponse response = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get movie by ID")
    public ResponseEntity<MovieResponse> getMovie(
            @Parameter(description = "Movie ID") 
            @PathVariable UUID id) {
        MovieResponse response = movieService.getMovie(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all movies with pagination")
    public ResponseEntity<Page<MovieResponse>> getAllMovies(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MovieResponse> movies = movieService.getAllMovies(pageable);
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/currently-playing")
    @Operation(summary = "Get currently playing movies")
    public ResponseEntity<Page<MovieResponse>> getCurrentlyPlayingMovies(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MovieResponse> movies = movieService.getCurrentlyPlayingMovies(pageable);
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming movies")
    public ResponseEntity<Page<MovieResponse>> getUpcomingMovies(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MovieResponse> movies = movieService.getUpcomingMovies(pageable);
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/search")
    @Operation(summary = "Search movies by keyword")
    public ResponseEntity<Page<MovieResponse>> searchMovies(
            @Parameter(description = "Search keyword") 
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MovieResponse> movies = movieService.searchMovies(keyword, pageable);
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/by-genres")
    @Operation(summary = "Get movies by genres")
    public ResponseEntity<Page<MovieResponse>> getMoviesByGenres(
            @Parameter(description = "Genres (comma-separated)") 
            @RequestParam Set<String> genres,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MovieResponse> movies = movieService.getMoviesByGenres(genres, pageable);
        return ResponseEntity.ok(movies);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a movie")
    public ResponseEntity<MovieResponse> updateMovie(
            @Parameter(description = "Movie ID") 
            @PathVariable UUID id,
            @Valid @RequestBody MovieRequest request) {
        MovieResponse response = movieService.updateMovie(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a movie")
    public ResponseEntity<Void> deleteMovie(
            @Parameter(description = "Movie ID") 
            @PathVariable UUID id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}