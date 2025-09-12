package com.moviebooking.booking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebooking.booking.client.PaymentServiceClient;
import com.moviebooking.booking.client.TheatreServiceClient;
import com.moviebooking.booking.domain.Booking;
import com.moviebooking.booking.domain.BookingStatus;
import com.moviebooking.booking.dto.*;
import com.moviebooking.common.events.booking.BookingConfirmedEvent;
import com.moviebooking.booking.exception.BookingException;
import com.moviebooking.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TheatreServiceClient theatreServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final RedissonClient redissonClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final int LOCK_WAIT_TIME = 10; // seconds
    private static final int LOCK_LEASE_TIME = 300; // 5 minutes

    @Transactional
    public BookingResponse bookTickets(BookingRequest request) {
        String bookingId = generateBookingId();
        String lockKey = generateLockKey(request.getShowId(), request.getSeatNumbers());
        
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // Try to acquire lock with 10 seconds wait time and 5 minutes lease time
            boolean lockAcquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            
            if (!lockAcquired) {
                log.warn("Failed to acquire lock for booking: {}", lockKey);
                throw new BookingException("Unable to process booking at this moment. Please try again.");
            }

            log.info("Lock acquired for booking: {} with key: {}", bookingId, lockKey);

            try {
                // Step 1: Check seat availability
                SeatAvailabilityResponse availability = checkSeatAvailability(request);
                
                if (!availability.isAvailable()) {
                    throw new BookingException("Requested seats are not available: " + availability.getUnavailableSeats());
                }

                // Step 2: Create pending booking
                Booking booking = createPendingBooking(request, bookingId, availability);
                booking = bookingRepository.save(booking);

                // Step 3: Process payment
                PaymentResponse paymentResponse = processPayment(booking);
                
                if (!"SUCCESS".equals(paymentResponse.getStatus())) {
                    // Update booking status to payment failed
                    booking.setStatus(BookingStatus.PAYMENT_FAILED);
                    bookingRepository.save(booking);
                    
                    throw new BookingException("Payment failed: " + paymentResponse.getMessage());
                }

                // Step 4: Confirm booking and publish event
                booking.setStatus(BookingStatus.CONFIRMED);
                booking.setPaymentId(paymentResponse.getPaymentId());
                booking = bookingRepository.save(booking);

                // Step 5: Publish booking confirmed event
                publishBookingConfirmedEvent(booking);

                log.info("Booking successfully created: {}", booking.getBookingId());
                
                return mapToBookingResponse(booking, "Booking confirmed successfully");

            } finally {
                // Always release the lock
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    log.info("Lock released for booking: {}", lockKey);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BookingException("Booking process interrupted");
        } catch (Exception e) {
            log.error("Error during booking process for request: {}", request, e);
            if (e instanceof BookingException) {
                throw e;
            }
            throw new BookingException("An error occurred while processing your booking: " + e.getMessage());
        }
    }

    private SeatAvailabilityResponse checkSeatAvailability(BookingRequest request) {
        try {
            SeatAvailabilityRequest availabilityRequest = SeatAvailabilityRequest.builder()
                    .showId(request.getShowId())
                    .seatNumbers(request.getSeatNumbers())
                    .build();

            var response = theatreServiceClient.checkSeatAvailability(availabilityRequest);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error checking seat availability for show: {}", request.getShowId(), e);
            throw new BookingException("Unable to verify seat availability. Please try again.");
        }
    }

    private PaymentResponse processPayment(Booking booking) {
        try {
            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .bookingId(booking.getBookingId())
                    .userId(booking.getUserId())
                    .amount(booking.getTotalAmount())
                    .description("Movie ticket booking for " + booking.getSeatNumbers().size() + " seats")
                    .build();

            var response = paymentServiceClient.processPayment(paymentRequest);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error processing payment for booking: {}", booking.getBookingId(), e);
            throw new BookingException("Payment processing failed. Please try again.");
        }
    }

    private void publishBookingConfirmedEvent(Booking booking) {
        try {
            BookingConfirmedEvent event = BookingConfirmedEvent.builder()
                    .bookingId(booking.getBookingId())
                    .userId(booking.getUserId())
                    .showId(booking.getShowId())
                    .theatreId(booking.getTheatreId())
                    .movieId(booking.getMovieId())
                    .seatNumbers(booking.getSeatNumbers())
                    .totalAmount(booking.getTotalAmount())
                    .paymentId(booking.getPaymentId())
                    .showDateTime(booking.getShowDateTime())
                    .confirmedAt(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("booking-confirmed", booking.getBookingId(), event);
            log.info("Published booking confirmed event for booking: {}", booking.getBookingId());
        } catch (Exception e) {
            log.error("Error publishing booking confirmed event for booking: {}", booking.getBookingId(), e);
            // Don't throw exception here as booking is already confirmed
        }
    }

    private Booking createPendingBooking(BookingRequest request, String bookingId, SeatAvailabilityResponse availability) {
        return Booking.builder()
                .bookingId(bookingId)
                .userId(request.getUserId())
                .showId(request.getShowId())
                .theatreId(availability.getTheatreId())
                .movieId(availability.getMovieId())
                .seatNumbers(request.getSeatNumbers())
                .totalAmount(availability.getTotalPrice())
                .status(BookingStatus.PENDING)
                .showDateTime(availability.getShowDateTime())
                .build();
    }

    private String generateBookingId() {
        return "BK-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateLockKey(Long showId, java.util.List<String> seatNumbers) {
        return "booking:lock:show:" + showId + ":seats:" + String.join(",", seatNumbers);
    }

    private BookingResponse mapToBookingResponse(Booking booking, String message) {
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .userId(booking.getUserId())
                .showId(booking.getShowId())
                .theatreId(booking.getTheatreId())
                .movieId(booking.getMovieId())
                .seatNumbers(booking.getSeatNumbers())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .paymentId(booking.getPaymentId())
                .showDateTime(booking.getShowDateTime())
                .createdAt(booking.getCreatedAt())
                .message(message)
                .build();
    }

    public BookingResponse getBookingByBookingId(String bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new BookingException("Booking not found with ID: " + bookingId));
        
        return mapToBookingResponse(booking, null);
    }
}