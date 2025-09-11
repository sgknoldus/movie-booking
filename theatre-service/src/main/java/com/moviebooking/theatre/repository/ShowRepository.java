package com.moviebooking.theatre.repository;

import com.moviebooking.theatre.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {
    List<Show> findByTheatreId(Long theatreId);
    
    List<Show> findByScreenId(Long screenId);
    
    List<Show> findByMovieId(Long movieId);
    
    @Query("SELECT s FROM Show s WHERE s.theatre.id = :theatreId AND s.showDateTime >= :startTime AND s.showDateTime <= :endTime")
    List<Show> findByTheatreIdAndShowDateTimeBetween(@Param("theatreId") Long theatreId,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT s FROM Show s WHERE s.movieId = :movieId AND s.theatre.city.id = :cityId AND s.showDateTime >= :startTime")
    List<Show> findByMovieIdAndCityIdAndShowDateTimeAfter(@Param("movieId") Long movieId,
                                                         @Param("cityId") Long cityId,
                                                         @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT s FROM Show s WHERE s.screen.id = :screenId AND " +
           "((s.showDateTime <= :endTime AND s.endDateTime >= :startTime))")
    List<Show> findConflictingShows(@Param("screenId") Long screenId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);
}