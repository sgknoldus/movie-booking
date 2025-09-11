package com.moviebooking.theatre.service;

import com.moviebooking.theatre.dto.SeatAvailabilityRequest;
import com.moviebooking.theatre.dto.SeatAvailabilityResponse;
import com.moviebooking.theatre.model.SeatAvailability;
import com.moviebooking.theatre.model.Show;
import com.moviebooking.theatre.repository.SeatAvailabilityRepository;
import com.moviebooking.theatre.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatAvailabilityService {

    private final SeatAvailabilityRepository seatAvailabilityRepository;
    private final ShowRepository showRepository;

    public SeatAvailabilityResponse checkSeatAvailability(SeatAvailabilityRequest request) {
        log.info("Checking seat availability for show: {} with seats: {}", 
                request.getShowId(), request.getSeatNumbers());

        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new RuntimeException("Show not found with id: " + request.getShowId()));

        List<String> availableSeats = new ArrayList<>();
        List<String> unavailableSeats = new ArrayList<>();

        // Check each requested seat
        for (String seatNumber : request.getSeatNumbers()) {
            SeatAvailability seatAvailability = seatAvailabilityRepository
                    .findByShowIdAndSeatNumber(request.getShowId(), seatNumber)
                    .orElse(null);

            if (seatAvailability == null || seatAvailability.isAvailable()) {
                availableSeats.add(seatNumber);
            } else {
                unavailableSeats.add(seatNumber);
            }
        }

        boolean allAvailable = unavailableSeats.isEmpty();
        BigDecimal pricePerSeat = show.getPrice();
        BigDecimal totalPrice = pricePerSeat.multiply(BigDecimal.valueOf(availableSeats.size()));

        return SeatAvailabilityResponse.builder()
                .available(allAvailable)
                .availableSeats(availableSeats)
                .unavailableSeats(unavailableSeats)
                .pricePerSeat(pricePerSeat)
                .totalPrice(totalPrice)
                .theatreId(show.getTheatre().getId())
                .movieId(show.getMovieId())
                .showDateTime(show.getShowDateTime())
                .message(allAvailable ? "All seats are available" : 
                        "Some seats are not available: " + String.join(", ", unavailableSeats))
                .build();
    }

    @Transactional
    public void markSeatsAsBooked(Long showId, List<String> seatNumbers) {
        log.info("Marking seats as booked for show: {} with seats: {}", showId, seatNumbers);

        for (String seatNumber : seatNumbers) {
            SeatAvailability seatAvailability = seatAvailabilityRepository
                    .findByShowIdAndSeatNumber(showId, seatNumber)
                    .orElse(SeatAvailability.builder()
                            .showId(showId)
                            .seatNumber(seatNumber)
                            .status(SeatAvailability.SeatStatus.AVAILABLE)
                            .build());

            seatAvailability.setAvailable(false);
            seatAvailabilityRepository.save(seatAvailability);
        }

        log.info("Successfully marked {} seats as booked for show: {}", seatNumbers.size(), showId);
    }

    @Transactional
    public void releaseSeats(Long showId, List<String> seatNumbers) {
        log.info("Releasing seats for show: {} with seats: {}", showId, seatNumbers);

        List<SeatAvailability> seatAvailabilities = seatNumbers.stream()
                .map(seatNumber -> seatAvailabilityRepository
                        .findByShowIdAndSeatNumber(showId, seatNumber)
                        .orElse(null))
                .filter(seat -> seat != null)
                .collect(Collectors.toList());

        seatAvailabilities.forEach(seat -> seat.setAvailable(true));
        seatAvailabilityRepository.saveAll(seatAvailabilities);

        log.info("Successfully released {} seats for show: {}", seatNumbers.size(), showId);
    }
}