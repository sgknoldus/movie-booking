package com.moviebooking.booking.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "show_id", nullable = false)
    private UUID showId;

    @Column(name = "theatre_id", nullable = false)
    private UUID theatreId;

    @Column(name = "movie_id", nullable = false)
    private UUID movieId;

    @ElementCollection
    @CollectionTable(name = "booking_seats", 
            joinColumns = @JoinColumn(name = "booking_id"))
    @Column(name = "seat_number")
    private List<String> seats;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "booked_at")
    private LocalDateTime bookedAt;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Version
    private Long version;

    public enum BookingStatus {
        PENDING,
        CONFIRMED,
        CANCELLED,
        PAYMENT_FAILED
    }
}
