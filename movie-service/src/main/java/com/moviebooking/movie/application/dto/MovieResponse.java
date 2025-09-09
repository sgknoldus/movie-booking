package com.moviebooking.movie.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class MovieResponse {
    private UUID id;
    private String title;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Set<String> genres;
    private Set<String> languages;
    private String posterUrl;
    private String trailerUrl;
    private Double rating;
}
