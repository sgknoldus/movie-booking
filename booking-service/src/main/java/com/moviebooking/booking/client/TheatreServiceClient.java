package com.moviebooking.booking.client;

import com.moviebooking.booking.dto.SeatAvailabilityRequest;
import com.moviebooking.booking.dto.SeatAvailabilityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "theatre-service", path = "/api/theatre")
public interface TheatreServiceClient {
    
    @PostMapping("/seats/check-availability")
    ResponseEntity<SeatAvailabilityResponse> checkSeatAvailability(@RequestBody SeatAvailabilityRequest request);
}