package com.moviebooking.ticket.repository;

import com.moviebooking.ticket.domain.Ticket;
import com.moviebooking.ticket.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    Optional<Ticket> findByTicketId(String ticketId);
    
    Optional<Ticket> findByBookingId(String bookingId);
    
    List<Ticket> findByUserIdAndStatus(Long userId, TicketStatus status);
    
    List<Ticket> findByUserId(Long userId);
    
    List<Ticket> findByShowId(Long showId);
    
    boolean existsByBookingId(String bookingId);
}