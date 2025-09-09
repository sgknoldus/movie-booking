package com.moviebooking.theatre.infrastructure.repository;

import com.moviebooking.theatre.domain.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShowRepository extends JpaRepository<Show, UUID> {
    
    List<Show> findByMovieIdAndShowTimeBetween(UUID movieId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT s FROM Show s WHERE s.id = :showId")
    Optional<Show> findByIdWithLock(@Param("showId") UUID showId);
    
    @Query("SELECT s FROM Show s " +
           "JOIN FETCH s.screen scr " +
           "JOIN FETCH scr.theatre t " +
           "WHERE s.id = :showId")
    Optional<Show> findByIdWithTheatre(@Param("showId") UUID showId);
}
