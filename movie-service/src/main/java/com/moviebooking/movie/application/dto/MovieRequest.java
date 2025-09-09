package com.moviebooking.movie.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class MovieRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Release date is required")
    private LocalDate releaseDate;
    
    @NotNull(message = "Duration is required")
    private Integer duration;
    
    private Set<String> genres;
    private Set<String> languages;
    private String posterUrl;
    private String trailerUrl;
}
