package com.moviebooking.movie.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    private Integer duration;

    @ElementCollection
    @CollectionTable(name = "movie_genres", 
            joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "genre")
    @Builder.Default
    private Set<String> genres = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "movie_languages", 
            joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "language")
    @Builder.Default
    private Set<String> languages = new HashSet<>();

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(name = "trailer_url")
    private String trailerUrl;

    @Column(name = "rating")
    private Double rating;

    @Version
    private Long version;
}
