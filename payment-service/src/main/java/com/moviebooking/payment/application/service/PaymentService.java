package com.moviebooking.payment.application.service;

import com.moviebooking.payment.application.dto.PaymentRequest;
import com.moviebooking.payment.application.dto.PaymentResponse;
import com.moviebooking.payment.domain.Payment;
import com.moviebooking.payment.infrastructure.client.BookingServiceClient;
import com.moviebooking.payment.infrastructure.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingServiceClient bookingServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(Payment.PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);

        return createPaymentResponse(payment);
    }

    @Transactional
    public PaymentResponse processPayment(UUID paymentId) {
        Payment payment = paymentRepository.findByIdWithLock(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Mock payment processing
        payment.setStatus(Payment.PaymentStatus.PROCESSING);
        payment = paymentRepository.save(payment);

        // Simulate payment processing
        boolean isSuccessful = processWithPaymentGateway(payment);

        if (isSuccessful) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setTransactionId(UUID.randomUUID().toString());
            
            // Update booking status
            bookingServiceClient.updatePaymentStatus(
                payment.getBookingId(),
                new BookingServiceClient.PaymentStatusUpdate("CONFIRMED")
            );

            // Publish payment completed event
            kafkaTemplate.send("payment-completed", new PaymentCompletedEvent(
                payment.getId(),
                payment.getBookingId(),
                payment.getAmount()
            ));
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            
            bookingServiceClient.updatePaymentStatus(
                payment.getBookingId(),
                new BookingServiceClient.PaymentStatusUpdate("PAYMENT_FAILED")
            );

            // Publish payment failed event
            kafkaTemplate.send("payment-failed", new PaymentFailedEvent(
                payment.getId(),
                payment.getBookingId()
            ));
        }

        payment = paymentRepository.save(payment);
        return createPaymentResponse(payment);
    }

    private boolean processWithPaymentGateway(Payment payment) {
        // Mock implementation - in real world, integrate with payment gateway
        return Math.random() < 0.9; // 90% success rate
    }

    private PaymentResponse createPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBookingId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .paymentUrl("/api/payments/" + payment.getId() + "/process")
                .build();
    }

    record PaymentCompletedEvent(UUID paymentId, UUID bookingId, BigDecimal amount) {}
    record PaymentFailedEvent(UUID paymentId, UUID bookingId) {}
}
