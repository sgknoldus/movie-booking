package com.moviebooking.booking.client;

import com.moviebooking.booking.dto.PaymentRequest;
import com.moviebooking.booking.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", path = "/api/v1/payments")
public interface PaymentServiceClient {

    @PostMapping
    ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request);
}