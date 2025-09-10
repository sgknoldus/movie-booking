package com.moviebooking.movie.application.service;

import com.moviebooking.movie.application.dto.MovieRequest;
import com.moviebooking.movie.application.dto.MovieResponse;
import com.moviebooking.movie.domain.Movie;
import com.moviebooking.movie.infrastructure.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;

    @Transactional
    public MovieResponse createMovie(MovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .releaseDate(request.getReleaseDate())
                .duration(request.getDuration())
                .genres(request.getGenres())
                .languages(request.getLanguages())
                .posterUrl(request.getPosterUrl())
                .trailerUrl(request.getTrailerUrl())
                .rating(0.0) // Default rating
                .build();

        movie = movieRepository.save(movie);
        return mapToResponse(movie);
    }

    @Transactional(readOnly = true)
    public MovieResponse getMovie(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
        return mapToResponse(movie);
    }

    @Transactional(readOnly = true)
    public Page<MovieResponse> getAllMovies(Pageable pageable) {
        return movieRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<MovieResponse> getCurrentlyPlayingMovies(Pageable pageable) {
        return movieRepository.findCurrentlyPlaying(LocalDate.now(), pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<MovieResponse> getUpcomingMovies(Pageable pageable) {
        return movieRepository.findUpcoming(LocalDate.now(), pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<MovieResponse> searchMovies(String keyword, Pageable pageable) {
        return movieRepository.search(keyword, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<MovieResponse> getMoviesByGenres(Set<String> genres, Pageable pageable) {
        return movieRepository.findByGenresIn(genres, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public MovieResponse updateMovie(UUID id, MovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));

        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setDuration(request.getDuration());
        movie.setGenres(request.getGenres());
        movie.setLanguages(request.getLanguages());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setTrailerUrl(request.getTrailerUrl());

        movie = movieRepository.save(movie);
        return mapToResponse(movie);
    }

    @Transactional
    public void deleteMovie(UUID id) {
        if (!movieRepository.existsById(id)) {
            throw new RuntimeException("Movie not found with id: " + id);
        }
        movieRepository.deleteById(id);
    }

    private MovieResponse mapToResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .releaseDate(movie.getReleaseDate())
                .duration(movie.getDuration())
                .genres(movie.getGenres())
                .languages(movie.getLanguages())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .rating(movie.getRating())
                .build();
    }
}