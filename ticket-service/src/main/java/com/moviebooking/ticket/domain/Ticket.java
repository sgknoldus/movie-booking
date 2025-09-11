package com.moviebooking.ticket.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tickets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String ticketId;
    
    @Column(nullable = false)
    private String bookingId;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Long showId;
    
    @Column(nullable = false)
    private Long theatreId;
    
    @Column(nullable = false)
    private Long movieId;
    
    @ElementCollection
    @CollectionTable(name = "ticket_seats", joinColumns = @JoinColumn(name = "ticket_id"))
    @Column(name = "seat_number")
    private List<String> seatNumbers;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    private String paymentId;
    
    @Column(nullable = false)
    private LocalDateTime showDateTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;
    
    private String qrCode;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}