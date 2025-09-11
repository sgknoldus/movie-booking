package com.moviebooking.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String paymentId;
    private String bookingId;
    private BigDecimal amount;
    private String status; // SUCCESS, FAILED, PENDING
    private String message;
    private LocalDateTime processedAt;
}