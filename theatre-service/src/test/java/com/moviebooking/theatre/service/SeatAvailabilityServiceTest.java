package com.moviebooking.theatre.service;

import com.moviebooking.theatre.dto.SeatAvailabilityRequest;
import com.moviebooking.theatre.dto.SeatAvailabilityResponse;
import com.moviebooking.theatre.model.SeatAvailability;
import com.moviebooking.theatre.model.Show;
import com.moviebooking.theatre.model.Theatre;
import com.moviebooking.theatre.repository.SeatAvailabilityRepository;
import com.moviebooking.theatre.repository.ShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatAvailabilityServiceTest {

    @Mock
    private SeatAvailabilityRepository seatAvailabilityRepository;

    @Mock
    private ShowRepository showRepository;

    @InjectMocks
    private SeatAvailabilityService seatAvailabilityService;

    private Show testShow;
    private Theatre testTheatre;
    private SeatAvailabilityRequest availabilityRequest;
    private SeatAvailability availableSeat;
    private SeatAvailability bookedSeat;

    @BeforeEach
    void setUp() {
        testTheatre = new Theatre();
        testTheatre.setId(1L);
        testTheatre.setName("PVR Cinemas");

        testShow = new Show();
        testShow.setId(1L);
        testShow.setMovieId(1L);
        testShow.setMovieTitle("Avengers");
        testShow.setPrice(BigDecimal.valueOf(250.00));
        testShow.setShowDateTime(LocalDateTime.now().plusHours(2));
        testShow.setTheatre(testTheatre);

        availabilityRequest = SeatAvailabilityRequest.builder()
                .showId(1L)
                .seatNumbers(List.of("A1", "A2", "A3"))
                .build();

        availableSeat = SeatAvailability.builder()
                .id(1L)
                .showId(1L)
                .seatNumber("A1")
                .status(SeatAvailability.SeatStatus.AVAILABLE)
                .build();
        availableSeat.setAvailable(true);

        bookedSeat = SeatAvailability.builder()
                .id(2L)
                .showId(1L)
                .seatNumber("A2")
                .status(SeatAvailability.SeatStatus.BOOKED)
                .build();
        bookedSeat.setAvailable(false);
    }

    @Test
    void checkSeatAvailability_ShouldReturnAllAvailable_WhenAllSeatsAreAvailable() {
        // Given
        when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));
        when(seatAvailabilityRepository.findByShowIdAndSeatNumber(1L, "A1"))
                .thenReturn(Optional.of(availableSeat));
        when(seatAvailabilityRepository.findByShowIdAndSeatNumber(1L, "A2"))
                .thenReturn(Optional.empty()); // No record = available
        when(seatAvailabilityRepository.findByShowIdAndSeatNumber(1L, "A3"))
                .thenReturn(Optional.of(availableSeat));

        // When
        SeatAvailabilityResponse response = seatAvailabilityService.checkSeatAvailability(availabilityRequest);

        // Then
        assertThat(response.isAvailable()).isTrue();
        assertThat(response.getAvailableSeats()).hasSize(3);
        assertThat(response.getUnavailableSeats()).isEmpty();
        assertThat(response.getTotalPrice()).isEqualTo(BigDecimal.valueOf(750.00)); // 3 * 250
        assertThat(response.getPricePerSeat()).isEqualTo(BigDecimal.valueOf(250.00));
        assertThat(response.getTheatreId()).isEqualTo(1L);
        assertThat(response.getMovieId()).isEqualTo(1L);
        assertThat(response.getMessage()).isEqualTo("All seats are available");

        verify(showRepository).findById(1L);
        verify(seatAvailabilityRepository, times(3)).findByShowIdAndSeatNumber(eq(1L), anyString());
    }

    @Test
    void checkSeatAvailability_ShouldReturnMixedAvailability_WhenSomeSeatsUnavailable() {
        // Given
        when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));
        when(seatAvailabilityRepository.findByShowIdAndSeatNumber(1L, "A1"))
                .thenReturn(Optional.of(availableSeat));
        when(seatAvailabilityRepository.findByShowIdAndSeatNumber(1L, "A2"))
                .thenReturn(Optional.of(bookedSeat));
        when(seatAvailabilityRepository.findByShowIdAndSeatNumber(1L, "A3"))
                .thenReturn(Optional.empty()); // Available

        // When
        SeatAvailabilityResponse response = seatAvailabilityService.checkSeatAvailability(availabilityRequest);

        // Then
        assertThat(response.isAvailable()).isFalse();
        assertThat(response.getAvailableSeats()).containsExactlyInAnyOrder("A1", "A3");
        assertThat(response.getUnavailableSeats()).containsExactly("A2");
        assertThat(response.getTotalPrice()).isEqualTo(BigDecimal.valueOf(500.00)); // 2 * 250
        assertThat(response.getMessage()).contains("Some seats are not available: A2");
    }

    @Test
    void checkSeatAvailability_ShouldThrowException_WhenShowNotFound() {
        // Given
        when(showRepository.findById(1L)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> seatAvailabilityService.checkSeatAvailability(availabilityRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Show not found with id: 1");

        verify(seatAvailabilityRepository, never()).findByShowIdAndSeatNumber(anyLong(), anyString());
    }

    @Test
    void checkSeatAvailability_ShouldHandleEmptySeatList() {
        // Given
        availabilityRequest.setSeatNumbers(List.of());
        when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));

        // When
        SeatAvailabilityResponse response = seatAvailabilityService.checkSeatAvailability(availabilityRequest);

        // Then
        assertThat(response.isAvailable()).isTrue();
        assertThat(response.getAvailableSeats()).isEmpty();
        assertThat(response.getUnavailableSeats()).isEmpty();
        assertThat(response.getTotalPrice().compareTo(BigDecimal.ZERO)).isEqualTo(0);
        assertThat(response.getMessage()).isEqualTo("All seats are available");

        verify(seatAvailabilityRepository, never()).findByShowIdAndSeatNumber(anyLong(), anyString());
    }

    @Test
    void markSeatsAsBooked_ShouldBookAvailableSeats() {
        // Given
        Long showId = 1L;
        List<String> seatNumbers = List.of("A1", "A2");

        when(seatAvailabilityRepository.findByShowIdAndSeatNumber(showId, "A1"))
                .thenReturn(Optional.of(availableSeat));
        when(seatAvailabilityRepository.findByShowIdAndSeatNumber(showId, "A2"))
                .thenReturn(Optional.empty()); // Create new seat availability

        // When
        seatAvailabilityService.markSeatsAsBooked(showId, seatNumbers);

        // Then
        ArgumentCaptor<SeatAvailability> seatCaptor = ArgumentCaptor.forClass(SeatAvailability.class);
        verify(seatAvailabilityRepository, times(2)).save(seatCaptor.capture());

        List<SeatAvailability> savedSeats = seatCaptor.getAllValues();
        assertThat(savedSeats).hasSize(2);
        savedSeats.forEach(seat -> assertThat(seat.isAvailable()).isFalse());

        // Verify existing seat was updated
        assertThat(availableSeat.isAvailable()).isFalse();
    }

    @Test
    void markSeatsAsBooked_ShouldCreateNewSeatAvailability_WhenNotExists() {
        // Given
        Long showId = 1L;
        List<String> seatNumbers = List.of("B1", "B2");

        when(seatAvailabilityRepository.findByShowIdAndSeatNumber(showId, "B1"))
                .thenReturn(Optional.empty());
        when(seatAvailabilityRepository.findByShowIdAndSeatNumber(showId, "B2"))
                .thenReturn(Optional.empty());

        // When
        seatAvailabilityService.markSeatsAsBooked(showId, seatNumbers);

        // Then
        ArgumentCaptor<SeatAvailability> seatCaptor = ArgumentCaptor.forClass(SeatAvailability.class);
        verify(seatAvailabilityRepository, times(2)).save(seatCaptor.capture());

        List<SeatAvailability> savedSeats = seatCaptor.getAllValues();
        assertThat(savedSeats).hasSize(2);

        SeatAvailability seat1 = savedSeats.get(0);
        assertThat(seat1.getShowId()).isEqualTo(showId);
        assertThat(seat1.getSeatNumber()).isEqualTo("B1");
        assertThat(seat1.isAvailable()).isFalse();
        assertThat(seat1.getStatus()).isEqualTo(SeatAvailability.SeatStatus.BOOKED);

        SeatAvailability seat2 = savedSeats.get(1);
        assertThat(seat2.getShowId()).isEqualTo(showId);
        assertThat(seat2.getSeatNumber()).isEqualTo("B2");
        assertThat(seat2.isAvailable()).isFalse();
    }

    @Test
    void releaseSeats_ShouldReleaseBookedSeats() {
        // Given
        Long showId = 1L;
        List<String> seatNumbers = List.of("A1", "A2");

        SeatAvailability bookedSeat1 = SeatAvailability.builder()
                .id(1L)
                .showId(showId)
                .seatNumber("A1")
                .status(SeatAvailability.SeatStatus.BOOKED)
                .build();
        bookedSeat1.setAvailable(false);

        SeatAvailability bookedSeat2 = SeatAvailability.builder()
                .id(2L)
                .showId(showId)
                .seatNumber("A2")
                .status(SeatAvailability.SeatStatus.BOOKED)
                .build();
        bookedSeat2.setAvailable(false);

        when(seatAvailabilityRepository.findByShowIdAndSeatNumber(showId, "A1"))
                .thenReturn(Optional.of(bookedSeat1));
        when(seatAvailabilityRepository.findByShowIdAndSeatNumber(showId, "A2"))
                .thenReturn(Optional.of(bookedSeat2));

        // When
        seatAvailabilityService.releaseSeats(showId, seatNumbers);

        // Then
        verify(seatAvailabilityRepository).saveAll(List.of(bookedSeat1, bookedSeat2));

        assertThat(bookedSeat1.isAvailable()).isTrue();
        assertThat(bookedSeat2.isAvailable()).isTrue();
    }

    @Test
    void releaseSeats_ShouldIgnoreNonExistentSeats() {
        // Given
        Long showId = 1L;
        List<String> seatNumbers = List.of("A1", "A2", "A3");

        SeatAvailability existingSeat = SeatAvailability.builder()
                .id(1L)
                .showId(showId)
                .seatNumber("A1")
                .status(SeatAvailability.SeatStatus.BOOKED)
                .build();
        existingSeat.setAvailable(false);

        when(seatAvailabilityRepository.findByShowIdAndSeatNumber(showId, "A1"))
                .thenReturn(Optional.of(existingSeat));
        when(seatAvailabilityRepository.findByShowIdAndSeatNumber(showId, "A2"))
                .thenReturn(Optional.empty()); // Non-existent seat
        when(seatAvailabilityRepository.findByShowIdAndSeatNumber(showId, "A3"))
                .thenReturn(Optional.empty()); // Non-existent seat

        // When
        seatAvailabilityService.releaseSeats(showId, seatNumbers);

        // Then
        verify(seatAvailabilityRepository).saveAll(List.of(existingSeat));
        assertThat(existingSeat.isAvailable()).isTrue();
    }

    @Test
    void releaseSeats_ShouldHandleEmptySeats() {
        // Given
        Long showId = 1L;
        List<String> seatNumbers = List.of();

        // When
        seatAvailabilityService.releaseSeats(showId, seatNumbers);

        // Then
        verify(seatAvailabilityRepository).saveAll(List.of());
        verify(seatAvailabilityRepository, never()).findByShowIdAndSeatNumber(anyLong(), anyString());
    }

    @Test
    void markSeatsAsBooked_ShouldHandleEmptySeats() {
        // Given
        Long showId = 1L;
        List<String> seatNumbers = List.of();

        // When
        seatAvailabilityService.markSeatsAsBooked(showId, seatNumbers);

        // Then
        verify(seatAvailabilityRepository, never()).findByShowIdAndSeatNumber(anyLong(), anyString());
        verify(seatAvailabilityRepository, never()).save(any());
    }

    @Test
    void checkSeatAvailability_ShouldCalculateCorrectPriceForZeroAvailableSeats() {
        // Given
        when(showRepository.findById(1L)).thenReturn(Optional.of(testShow));
        when(seatAvailabilityRepository.findByShowIdAndSeatNumber(1L, "A1"))
                .thenReturn(Optional.of(bookedSeat));
        when(seatAvailabilityRepository.findByShowIdAndSeatNumber(1L, "A2"))
                .thenReturn(Optional.of(bookedSeat));
        when(seatAvailabilityRepository.findByShowIdAndSeatNumber(1L, "A3"))
                .thenReturn(Optional.of(bookedSeat));

        // When
        SeatAvailabilityResponse response = seatAvailabilityService.checkSeatAvailability(availabilityRequest);

        // Then
        assertThat(response.isAvailable()).isFalse();
        assertThat(response.getAvailableSeats()).isEmpty();
        assertThat(response.getUnavailableSeats()).hasSize(3);
        assertThat(response.getTotalPrice().compareTo(BigDecimal.ZERO)).isEqualTo(0);
        assertThat(response.getMessage()).contains("Some seats are not available");
    }
}