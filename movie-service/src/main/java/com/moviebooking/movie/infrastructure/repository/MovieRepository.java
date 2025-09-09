package com.moviebooking.movie.infrastructure.repository;

import com.moviebooking.movie.domain.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface MovieRepository extends JpaRepository<Movie, UUID> {
    
    @Query("SELECT m FROM Movie m WHERE m.releaseDate <= :now ORDER BY m.releaseDate DESC")
    Page<Movie> findCurrentlyPlaying(@Param("now") LocalDate now, Pageable pageable);
    
    @Query("SELECT m FROM Movie m WHERE m.releaseDate > :now ORDER BY m.releaseDate ASC")
    Page<Movie> findUpcoming(@Param("now") LocalDate now, Pageable pageable);
    
    Page<Movie> findByGenresIn(Set<String> genres, Pageable pageable);
    
    @Query("SELECT DISTINCT m FROM Movie m " +
           "WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Movie> search(@Param("keyword") String keyword, Pageable pageable);
    
    List<Movie> findByLanguagesIn(Set<String> languages);
}
