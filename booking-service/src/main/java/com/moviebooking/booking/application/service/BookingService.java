package com.moviebooking.booking.application.service;

import com.moviebooking.booking.application.dto.BookingRequest;
import com.moviebooking.booking.application.dto.BookingResponse;
import com.moviebooking.booking.domain.Booking;
import com.moviebooking.booking.infrastructure.client.TheatreServiceClient;
import com.moviebooking.booking.infrastructure.repository.BookingRepository;
import com.moviebooking.common.events.booking.SeatBookedEvent;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TheatreServiceClient theatreServiceClient;
    private final RedissonClient redissonClient;
    private final KafkaTemplate<String, SeatBookedEvent> kafkaTemplate;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) throws Exception {
        String lockKey = "show-lock:" + request.getShowId();
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // Try to acquire the lock with a timeout
            boolean isLocked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RuntimeException("Unable to acquire lock for booking");
            }

            // Get show details and validate seats
            var show = theatreServiceClient.getShow(request.getShowId());
            validateSeatsAvailability(show, request.getSeats());

            // Calculate total amount
            BigDecimal totalAmount = calculateTotalAmount(show.price(), request.getSeats().size());

            // Create booking
            Booking booking = Booking.builder()
                    .id(UUID.randomUUID())
                    .userId(request.getUserId())
                    .showId(request.getShowId())
                    .theatreId(request.getTheatreId())
                    .movieId(request.getMovieId())
                    .seats(request.getSeats())
                    .totalAmount(totalAmount)
                    .status(Booking.BookingStatus.PENDING)
                    .bookedAt(LocalDateTime.now())
                    .build();

            booking = bookingRepository.save(booking);

            // Publish seat booked event
            SeatBookedEvent event = new SeatBookedEvent(
                    booking.getId(),
                    booking.getUserId(),
                    booking.getShowId(),
                    booking.getSeats(),
                    booking.getTotalAmount().doubleValue()
            );
            kafkaTemplate.send("seat-booked", event);

            return createBookingResponse(booking);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void validateSeatsAvailability(TheatreServiceClient.ShowResponse show, List<String> requestedSeats) {
        if (show == null || show.availableSeats() == null || requestedSeats == null) {
            throw new IllegalArgumentException("Show, available seats, or requested seats cannot be null");
        }

        List<String> availableSeats;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            availableSeats = objectMapper.readValue(show.availableSeats(), new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Error parsing available seats: " + e.getMessage());
        }
        
        // Check if all requested seats are available
        if (!availableSeats.containsAll(requestedSeats)) {
            List<String> unavailableSeats = requestedSeats.stream()
                    .filter(seat -> !availableSeats.contains(seat))
                    .collect(Collectors.toList());
                    
            throw new IllegalArgumentException("Following seats are not available: " + String.join(", ", unavailableSeats));
        }
    }

    private BigDecimal calculateTotalAmount(Double price, int seatCount) {
        return BigDecimal.valueOf(price * seatCount);
    }

    private BookingResponse createBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .bookingId(booking.getId())
                .showId(booking.getShowId())
                .theatreId(booking.getTheatreId())
                .movieId(booking.getMovieId())
                .seats(booking.getSeats())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .bookedAt(booking.getBookedAt())
                .paymentUrl("/api/payments/" + booking.getId()) // Mock payment URL
                .build();
    }
}
