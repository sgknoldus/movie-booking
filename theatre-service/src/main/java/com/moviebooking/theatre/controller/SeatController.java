package com.moviebooking.theatre.controller;

import com.moviebooking.theatre.dto.SeatAvailabilityRequest;
import com.moviebooking.theatre.dto.SeatAvailabilityResponse;
import com.moviebooking.theatre.service.SeatAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/theatre/seats")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Seat Management", description = "Seat availability management operations")
public class SeatController {

    private final SeatAvailabilityService seatAvailabilityService;

    @PostMapping("/check-availability")
    @Operation(summary = "Check seat availability", description = "Check availability of requested seats for a show")
    public ResponseEntity<SeatAvailabilityResponse> checkSeatAvailability(@RequestBody SeatAvailabilityRequest request) {
        log.info("Checking seat availability for show: {} with seats: {}", 
                request.getShowId(), request.getSeatNumbers());
        
        SeatAvailabilityResponse response = seatAvailabilityService.checkSeatAvailability(request);
        return ResponseEntity.ok(response);
    }
}