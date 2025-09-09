package com.moviebooking.booking.infrastructure.repository;

import com.moviebooking.booking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdWithLock(@Param("id") UUID id);
    
    List<Booking> findByUserIdOrderByBookedAtDesc(UUID userId);
    
    List<Booking> findByShowId(UUID showId);
    
    @Query("SELECT b FROM Booking b WHERE b.showId = :showId AND b.status = 'CONFIRMED'")
    List<Booking> findConfirmedBookingsByShowId(@Param("showId") UUID showId);
}
