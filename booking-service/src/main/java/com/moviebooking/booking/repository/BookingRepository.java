package com.moviebooking.booking.repository;

import com.moviebooking.booking.domain.Booking;
import com.moviebooking.booking.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    Optional<Booking> findByBookingId(String bookingId);
    
    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);
    
    List<Booking> findByUserId(Long userId);
    
    @Query("SELECT b FROM Booking b WHERE b.showId = :showId AND b.status IN :statuses")
    List<Booking> findByShowIdAndStatusIn(@Param("showId") Long showId, @Param("statuses") List<BookingStatus> statuses);
    
    boolean existsByShowIdAndSeatNumbersContainingAndStatusIn(Long showId, String seatNumber, List<BookingStatus> statuses);
}