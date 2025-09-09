package com.moviebooking.payment.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "booking-service")
public interface BookingServiceClient {
    
    @PutMapping("/api/bookings/{bookingId}/payment-status")
    void updatePaymentStatus(@PathVariable UUID bookingId, @RequestBody PaymentStatusUpdate update);

    record PaymentStatusUpdate(String status) {}
}
