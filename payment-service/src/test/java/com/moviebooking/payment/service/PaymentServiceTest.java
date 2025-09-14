package com.moviebooking.payment.service;

import com.moviebooking.payment.dto.PaymentRequest;
import com.moviebooking.payment.dto.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Spy
    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        paymentRequest = PaymentRequest.builder()
                .bookingId("BK-123456789-efgh5678")
                .userId(1L)
                .amount(BigDecimal.valueOf(500.00))
                .description("Movie ticket booking for 2 seats")
                .build();
    }

    @Test
    void processPayment_ShouldReturnSuccessResponse_WhenPaymentSucceeds() {
        // Given - Mock shouldPaymentSucceed to return true
        doReturn(true).when(paymentService).shouldPaymentSucceed();

        // When
        PaymentResponse response = paymentService.processPayment(paymentRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getBookingId()).isEqualTo("BK-123456789-efgh5678");
        assertThat(response.getAmount()).isEqualTo(BigDecimal.valueOf(500.00));
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getMessage()).isEqualTo("Payment processed successfully");
        assertThat(response.getPaymentId()).startsWith("PAY-");
        assertThat(response.getProcessedAt()).isNotNull();
    }

    @Test
    void processPayment_ShouldReturnFailureResponse_WhenPaymentFails() {
        // Given - Mock shouldPaymentSucceed to return false
        doReturn(false).when(paymentService).shouldPaymentSucceed();

        // When
        PaymentResponse response = paymentService.processPayment(paymentRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getBookingId()).isEqualTo("BK-123456789-efgh5678");
        assertThat(response.getAmount()).isEqualTo(BigDecimal.valueOf(500.00));
        assertThat(response.getStatus()).isEqualTo("FAILED");
        assertThat(response.getMessage()).isEqualTo("Payment failed due to insufficient funds");
        assertThat(response.getPaymentId()).startsWith("PAY-");
        assertThat(response.getProcessedAt()).isNotNull();
    }

    @Test
    void processPayment_ShouldGenerateUniquePaymentId() {
        // Given - Mock shouldPaymentSucceed to return true
        doReturn(true).when(paymentService).shouldPaymentSucceed();

        // When
        PaymentResponse response1 = paymentService.processPayment(paymentRequest);

        // Create a small delay to ensure different timestamp
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        PaymentResponse response2 = paymentService.processPayment(paymentRequest);

        // Then
        assertThat(response1.getPaymentId()).isNotEqualTo(response2.getPaymentId());
        assertThat(response1.getPaymentId()).startsWith("PAY-");
        assertThat(response2.getPaymentId()).startsWith("PAY-");
    }

    @Test
    void processPayment_ShouldSetProcessedAtTimestamp() {
        // Given
        LocalDateTime beforeProcessing = LocalDateTime.now();
        doReturn(true).when(paymentService).shouldPaymentSucceed();

        // When
        PaymentResponse response = paymentService.processPayment(paymentRequest);

        // Then
        assertThat(response.getProcessedAt()).isNotNull();
        assertThat(response.getProcessedAt()).isAfter(beforeProcessing);
        assertThat(response.getProcessedAt()).isBefore(LocalDateTime.now().plusMinutes(1));
    }

    @Test
    void processPayment_ShouldCopyAllRequestFields() {
        // Given
        PaymentRequest requestWithAllFields = PaymentRequest.builder()
                .bookingId("BK-999888777-test123")
                .userId(42L)
                .amount(BigDecimal.valueOf(1250.75))
                .description("Premium movie booking for 5 seats")
                .build();

        doReturn(true).when(paymentService).shouldPaymentSucceed();

        // When
        PaymentResponse response = paymentService.processPayment(requestWithAllFields);

        // Then
        assertThat(response.getBookingId()).isEqualTo("BK-999888777-test123");
        assertThat(response.getAmount()).isEqualTo(BigDecimal.valueOf(1250.75));
    }

    @Test
    void processPayment_ShouldHandleZeroAmount() {
        // Given
        paymentRequest.setAmount(BigDecimal.ZERO);
        doReturn(true).when(paymentService).shouldPaymentSucceed();

        // When
        PaymentResponse response = paymentService.processPayment(paymentRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualTo(BigDecimal.ZERO);
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    void processPayment_ShouldHandleLargeAmount() {
        // Given
        paymentRequest.setAmount(new BigDecimal("999999.99"));
        doReturn(true).when(paymentService).shouldPaymentSucceed();

        // When
        PaymentResponse response = paymentService.processPayment(paymentRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("999999.99"));
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    void processPayment_ShouldHandleNullBookingId() {
        // Given
        paymentRequest.setBookingId(null);
        doReturn(true).when(paymentService).shouldPaymentSucceed();

        // When
        PaymentResponse response = paymentService.processPayment(paymentRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getBookingId()).isNull();
        assertThat(response.getPaymentId()).isNotNull();
    }

    @Test
    void processPayment_ShouldHandleEmptyBookingId() {
        // Given
        paymentRequest.setBookingId("");
        doReturn(true).when(paymentService).shouldPaymentSucceed();

        // When
        PaymentResponse response = paymentService.processPayment(paymentRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getBookingId()).isEmpty();
        assertThat(response.getPaymentId()).isNotNull();
    }

    @Test
    void processPayment_ShouldSimulateProcessingDelay() {
        // Given
        long startTime = System.currentTimeMillis();
        doReturn(true).when(paymentService).shouldPaymentSucceed();

        // When
        PaymentResponse response = paymentService.processPayment(paymentRequest);
        long endTime = System.currentTimeMillis();

        // Then
        long processingTime = endTime - startTime;
        assertThat(processingTime).isGreaterThanOrEqualTo(900); // At least 900ms (accounting for test execution variance)
        assertThat(response).isNotNull();
    }

    @Test
    void processPayment_ShouldHandleInterruptedException() {
        // Given
        Thread.currentThread().interrupt(); // Set interrupt flag
        doReturn(true).when(paymentService).shouldPaymentSucceed();

        // When
        PaymentResponse response = paymentService.processPayment(paymentRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        // Verify interrupt flag is still set
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }

    @Test
    void processPayment_ShouldHandleNegativeAmount() {
        // Given
        paymentRequest.setAmount(BigDecimal.valueOf(-100.00));
        doReturn(true).when(paymentService).shouldPaymentSucceed();

        // When
        PaymentResponse response = paymentService.processPayment(paymentRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualTo(BigDecimal.valueOf(-100.00));
        assertThat(response.getStatus()).isEqualTo("SUCCESS"); // Service doesn't validate amount
    }
}