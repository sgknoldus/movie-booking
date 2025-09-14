package com.moviebooking.theatre.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebooking.theatre.model.*;
import com.moviebooking.theatre.repository.OutboxEventRepository;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class OutboxEventServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxEventService outboxEventService;

    private City testCity;
    private Theatre testTheatre;
    private Screen testScreen;
    private Show testShow;
    private SeatAvailability testSeat;
    private OutboxEvent testEvent;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        testCity = new City();
        testCity.setId(1L);
        testCity.setName("Mumbai");
        testCity.setState("Maharashtra");
        testCity.setCountry("India");
        testCity.setZipCode("400001");
        testCity.setCreatedAt(LocalDateTime.now());
        testCity.setUpdatedAt(LocalDateTime.now());

        testTheatre = new Theatre();
        testTheatre.setId(1L);
        testTheatre.setName("PVR Cinemas");
        testTheatre.setAddress("123 Main Street");
        testTheatre.setPhoneNumber("1234567890");
        testTheatre.setEmail("pvr@example.com");
        testTheatre.setLatitude(BigDecimal.valueOf(19.0760));
        testTheatre.setLongitude(BigDecimal.valueOf(72.8777));
        testTheatre.setCity(testCity);
        testTheatre.setCreatedAt(LocalDateTime.now());
        testTheatre.setUpdatedAt(LocalDateTime.now());

        testScreen = new Screen();
        testScreen.setId(1L);
        testScreen.setName("Screen 1");
        testScreen.setTotalSeats(100);
        testScreen.setScreenType(Screen.ScreenType.STANDARD);
        testScreen.setTheatre(testTheatre);
        testScreen.setCreatedAt(LocalDateTime.now());
        testScreen.setUpdatedAt(LocalDateTime.now());

        testShow = new Show();
        testShow.setId(1L);
        testShow.setMovieId(1L);
        testShow.setMovieTitle("Avengers");
        testShow.setShowDateTime(LocalDateTime.now().plusHours(2));
        testShow.setEndDateTime(LocalDateTime.now().plusHours(5));
        testShow.setPrice(BigDecimal.valueOf(250.00));
        testShow.setAvailableSeats(95);
        testShow.setStatus(Show.ShowStatus.SCHEDULED);
        testShow.setScreen(testScreen);
        testShow.setTheatre(testTheatre);
        testShow.setCreatedAt(LocalDateTime.now());
        testShow.setUpdatedAt(LocalDateTime.now());

        testSeat = SeatAvailability.builder()
                .id(1L)
                .seatNumber("A1")
                .rowNumber("1")
                .seatType(SeatAvailability.SeatType.REGULAR)
                .status(SeatAvailability.SeatStatus.AVAILABLE)
                .showId(1L)
                .show(testShow)
                .bookingId("BK-123")
                .lockedUntil(LocalDateTime.now().plusMinutes(15))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testEvent = new OutboxEvent();
        testEvent.setId(1L);
        testEvent.setAggregateId("1");
        testEvent.setAggregateType("City");
        testEvent.setEventType("CITY_CREATED");
        testEvent.setEventData("{\"id\":1,\"name\":\"Mumbai\"}");
        testEvent.setStatus(OutboxEvent.EventStatus.PENDING);
        testEvent.setRetryCount(0);
        testEvent.setCreatedAt(LocalDateTime.now());

        lenient().when(objectMapper.writeValueAsString(any())).thenReturn("{\"serialized\":\"data\"}");
    }

    @Test
    void publishCityEvent_ShouldCreateOutboxEvent() throws JsonProcessingException {
        // Given
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenReturn(testEvent);

        // When
        outboxEventService.publishCityEvent("CITY_CREATED", testCity);

        // Then
        ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(eventCaptor.capture());
        verify(objectMapper).writeValueAsString(any(Map.class));

        OutboxEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getAggregateId()).isEqualTo("1");
        assertThat(capturedEvent.getAggregateType()).isEqualTo("City");
        assertThat(capturedEvent.getEventType()).isEqualTo("CITY_CREATED");
        assertThat(capturedEvent.getStatus()).isEqualTo(OutboxEvent.EventStatus.PENDING);
    }

    @Test
    void publishTheatreEvent_ShouldCreateOutboxEvent() throws JsonProcessingException {
        // Given
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenReturn(testEvent);

        // When
        outboxEventService.publishTheatreEvent("THEATRE_CREATED", testTheatre);

        // Then
        ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(eventCaptor.capture());

        OutboxEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getAggregateId()).isEqualTo("1");
        assertThat(capturedEvent.getAggregateType()).isEqualTo("Theatre");
        assertThat(capturedEvent.getEventType()).isEqualTo("THEATRE_CREATED");
    }

    @Test
    void publishScreenEvent_ShouldCreateOutboxEvent() throws JsonProcessingException {
        // Given
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenReturn(testEvent);

        // When
        outboxEventService.publishScreenEvent("SCREEN_CREATED", testScreen);

        // Then
        ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(eventCaptor.capture());

        OutboxEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getAggregateId()).isEqualTo("1");
        assertThat(capturedEvent.getAggregateType()).isEqualTo("Screen");
        assertThat(capturedEvent.getEventType()).isEqualTo("SCREEN_CREATED");
    }

    @Test
    void publishShowEvent_ShouldCreateOutboxEvent() throws JsonProcessingException {
        // Given
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenReturn(testEvent);

        // When
        outboxEventService.publishShowEvent("SHOW_CREATED", testShow);

        // Then
        ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(eventCaptor.capture());

        OutboxEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getAggregateId()).isEqualTo("1");
        assertThat(capturedEvent.getAggregateType()).isEqualTo("Show");
        assertThat(capturedEvent.getEventType()).isEqualTo("SHOW_CREATED");
    }

    @Test
    void publishSeatAvailabilityEvent_ShouldCreateOutboxEvent() throws JsonProcessingException {
        // Given
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenReturn(testEvent);

        // When
        outboxEventService.publishSeatAvailabilityEvent("SEAT_BOOKED", testSeat);

        // Then
        ArgumentCaptor<OutboxEvent> eventCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(eventCaptor.capture());

        OutboxEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getAggregateId()).isEqualTo("1");
        assertThat(capturedEvent.getAggregateType()).isEqualTo("SeatAvailability");
        assertThat(capturedEvent.getEventType()).isEqualTo("SEAT_BOOKED");
    }

    @Test
    void publishEvent_ShouldThrowException_WhenJsonProcessingFails() throws JsonProcessingException {
        // Given
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Serialization failed") {});

        // Then
        assertThatThrownBy(() -> outboxEventService.publishCityEvent("CITY_CREATED", testCity))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to create outbox event")
                .hasCauseInstanceOf(JsonProcessingException.class);

        verify(outboxEventRepository, never()).save(any(OutboxEvent.class));
    }

    @Test
    void getPendingEvents_ShouldReturnPendingEvents() {
        // Given
        List<OutboxEvent> pendingEvents = List.of(testEvent);
        when(outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxEvent.EventStatus.PENDING))
                .thenReturn(pendingEvents);

        // When
        List<OutboxEvent> result = outboxEventService.getPendingEvents();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testEvent);
        verify(outboxEventRepository).findByStatusOrderByCreatedAtAsc(OutboxEvent.EventStatus.PENDING);
    }

    @Test
    void markEventAsProcessed_ShouldUpdateEventStatus() {
        // Given
        testEvent.setStatus(OutboxEvent.EventStatus.PENDING);
        when(outboxEventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenReturn(testEvent);

        // When
        outboxEventService.markEventAsProcessed(1L);

        // Then
        assertThat(testEvent.getStatus()).isEqualTo(OutboxEvent.EventStatus.PROCESSED);
        assertThat(testEvent.getProcessedAt()).isNotNull();
        verify(outboxEventRepository).save(testEvent);
    }

    @Test
    void markEventAsProcessed_ShouldDoNothing_WhenEventNotFound() {
        // Given
        when(outboxEventRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        outboxEventService.markEventAsProcessed(1L);

        // Then
        verify(outboxEventRepository, never()).save(any(OutboxEvent.class));
    }

    @Test
    void markEventAsFailed_ShouldUpdateEventStatusAndIncrementRetryCount() {
        // Given
        testEvent.setStatus(OutboxEvent.EventStatus.PENDING);
        testEvent.setRetryCount(0);
        when(outboxEventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenReturn(testEvent);

        // When
        outboxEventService.markEventAsFailed(1L);

        // Then
        assertThat(testEvent.getStatus()).isEqualTo(OutboxEvent.EventStatus.FAILED);
        assertThat(testEvent.getRetryCount()).isEqualTo(1);
        verify(outboxEventRepository).save(testEvent);
    }

    @Test
    void markEventAsFailed_ShouldLogError_WhenMaxRetryCountReached() {
        // Given
        testEvent.setRetryCount(2); // Will become 3 after increment
        when(outboxEventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenReturn(testEvent);

        // When
        outboxEventService.markEventAsFailed(1L);

        // Then
        assertThat(testEvent.getRetryCount()).isEqualTo(3);
        verify(outboxEventRepository).save(testEvent);
    }

    @Test
    void markEventAsFailed_ShouldDoNothing_WhenEventNotFound() {
        // Given
        when(outboxEventRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        outboxEventService.markEventAsFailed(1L);

        // Then
        verify(outboxEventRepository, never()).save(any(OutboxEvent.class));
    }

    @Test
    void cleanupProcessedEvents_ShouldDeleteOldProcessedEvents() {
        // When
        outboxEventService.cleanupProcessedEvents();

        // Then
        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(outboxEventRepository).deleteByStatusAndProcessedAtBefore(
                eq(OutboxEvent.EventStatus.PROCESSED),
                dateCaptor.capture()
        );

        LocalDateTime capturedDate = dateCaptor.getValue();
        LocalDateTime expectedDate = LocalDateTime.now().minusDays(7);
        // Allow 1 minute tolerance for test execution
        assertThat(capturedDate).isBetween(
                expectedDate.minusMinutes(1),
                expectedDate.plusMinutes(1)
        );
    }

    @Test
    void createCityEventData_ShouldContainCorrectFields() throws JsonProcessingException {
        // Given
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenReturn(testEvent);

        // When
        outboxEventService.publishCityEvent("CITY_CREATED", testCity);

        // Then
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(objectMapper).writeValueAsString(dataCaptor.capture());

        Map<String, Object> eventData = dataCaptor.getValue();
        assertThat(eventData).containsKeys("id", "name", "state", "country", "zipCode", "createdAt", "updatedAt");
        assertThat(eventData.get("id")).isEqualTo(1L);
        assertThat(eventData.get("name")).isEqualTo("Mumbai");
        assertThat(eventData.get("state")).isEqualTo("Maharashtra");
        assertThat(eventData.get("country")).isEqualTo("India");
        assertThat(eventData.get("zipCode")).isEqualTo("400001");
    }

    @Test
    void createTheatreEventData_ShouldContainCorrectFields() throws JsonProcessingException {
        // Given
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenReturn(testEvent);

        // When
        outboxEventService.publishTheatreEvent("THEATRE_CREATED", testTheatre);

        // Then
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(objectMapper).writeValueAsString(dataCaptor.capture());

        Map<String, Object> eventData = dataCaptor.getValue();
        assertThat(eventData).containsKeys(
                "id", "name", "address", "phoneNumber", "email",
                "latitude", "longitude", "cityId", "cityName",
                "createdAt", "updatedAt"
        );
        assertThat(eventData.get("id")).isEqualTo(1L);
        assertThat(eventData.get("name")).isEqualTo("PVR Cinemas");
        assertThat(eventData.get("cityId")).isEqualTo(1L);
        assertThat(eventData.get("cityName")).isEqualTo("Mumbai");
    }

    @Test
    void createSeatEventData_ShouldContainCorrectFields() throws JsonProcessingException {
        // Given
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenReturn(testEvent);

        // When
        outboxEventService.publishSeatAvailabilityEvent("SEAT_BOOKED", testSeat);

        // Then
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(objectMapper).writeValueAsString(dataCaptor.capture());

        Map<String, Object> eventData = dataCaptor.getValue();
        assertThat(eventData).containsKeys(
                "id", "seatNumber", "rowNumber", "seatType", "status",
                "bookingId", "lockedUntil", "showId", "createdAt", "updatedAt"
        );
        assertThat(eventData.get("id")).isEqualTo(1L);
        assertThat(eventData.get("seatNumber")).isEqualTo("A1");
        assertThat(eventData.get("bookingId")).isEqualTo("BK-123");
        assertThat(eventData.get("showId")).isEqualTo(1L);
    }
}