package com.moviebooking.theatre.repository;

import com.moviebooking.theatre.model.SeatAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeatAvailabilityRepository extends JpaRepository<SeatAvailability, Long> {
    List<SeatAvailability> findByShowId(Long showId);
    
    List<SeatAvailability> findByShowIdAndStatus(Long showId, SeatAvailability.SeatStatus status);
    
    @Query("SELECT s FROM SeatAvailability s WHERE s.show.id = :showId AND s.seatNumber = :seatNumber")
    java.util.Optional<SeatAvailability> findByShowIdAndSeatNumber(@Param("showId") Long showId, @Param("seatNumber") String seatNumber);
    
    @Query("SELECT COUNT(s) FROM SeatAvailability s WHERE s.show.id = :showId AND s.status = :status")
    Long countByShowIdAndStatus(@Param("showId") Long showId, @Param("status") SeatAvailability.SeatStatus status);
    
    @Modifying
    @Query("UPDATE SeatAvailability s SET s.status = 'AVAILABLE', s.bookingId = null, s.lockedUntil = null " +
           "WHERE s.status = 'LOCKED' AND s.lockedUntil < :currentTime")
    int releaseExpiredLocks(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT s FROM SeatAvailability s WHERE s.show.id = :showId AND s.seatNumber IN :seatNumbers")
    List<SeatAvailability> findByShowIdAndSeatNumberIn(@Param("showId") Long showId, @Param("seatNumbers") List<String> seatNumbers);
}