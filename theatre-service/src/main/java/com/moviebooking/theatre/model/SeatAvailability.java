package com.moviebooking.theatre.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "seat_availability", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"show_id", "seat_number"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class SeatAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "seat_number", nullable = false)
    private String seatNumber;
    
    @Column(name = "row_number", nullable = false)
    private String rowNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type")
    private SeatType seatType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;
    
    @Column(name = "booking_id")
    private String bookingId;
    
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    @Column(name = "show_id", nullable = false)
    private Long showId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false, insertable = false, updatable = false)
    private Show show;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum SeatType {
        REGULAR, PREMIUM, VIP, WHEELCHAIR_ACCESSIBLE
    }
    
    public enum SeatStatus {
        AVAILABLE, BOOKED, LOCKED, BLOCKED
    }
    
    public boolean isAvailable() {
        return this.status == SeatStatus.AVAILABLE;
    }
    
    public void setAvailable(boolean available) {
        this.status = available ? SeatStatus.AVAILABLE : SeatStatus.BOOKED;
    }
}