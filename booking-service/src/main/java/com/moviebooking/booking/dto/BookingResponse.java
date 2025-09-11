package com.moviebooking.booking.dto;

import com.moviebooking.booking.domain.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    
    private String bookingId;
    private Long userId;
    private Long showId;
    private Long theatreId;
    private Long movieId;
    private List<String> seatNumbers;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private String paymentId;
    private LocalDateTime showDateTime;
    private LocalDateTime createdAt;
    private String message;
}