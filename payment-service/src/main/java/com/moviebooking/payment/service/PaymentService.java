package com.moviebooking.payment.service;

import com.moviebooking.payment.dto.PaymentRequest;
import com.moviebooking.payment.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {

    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for booking: {} with amount: {}", 
                request.getBookingId(), request.getAmount());
        
        // Simulate payment processing time
        try {
            Thread.sleep(1000); // 1 second processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Dummy implementation - 90% success rate
        boolean isSuccess = shouldPaymentSucceed();
        
        String paymentId = generatePaymentId();
        String status = isSuccess ? "SUCCESS" : "FAILED";
        String message = isSuccess ? "Payment processed successfully" : "Payment failed due to insufficient funds";
        
        PaymentResponse response = PaymentResponse.builder()
                .paymentId(paymentId)
                .bookingId(request.getBookingId())
                .amount(request.getAmount())
                .status(status)
                .message(message)
                .processedAt(LocalDateTime.now())
                .build();
        
        log.info("Payment processing completed for booking: {} with status: {}", 
                request.getBookingId(), status);
        
        return response;
    }
    
    protected boolean shouldPaymentSucceed() {
        return Math.random() < 0.9;
    }

    private String generatePaymentId() {
        return "PAY-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}