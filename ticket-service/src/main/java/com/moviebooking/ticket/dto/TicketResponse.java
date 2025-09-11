package com.moviebooking.ticket.dto;

import com.moviebooking.ticket.domain.TicketStatus;
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
public class TicketResponse {
    
    private String ticketId;
    private String bookingId;
    private Long userId;
    private Long showId;
    private Long theatreId;
    private Long movieId;
    private List<String> seatNumbers;
    private BigDecimal totalAmount;
    private String paymentId;
    private LocalDateTime showDateTime;
    private TicketStatus status;
    private String qrCode;
    private LocalDateTime createdAt;
}