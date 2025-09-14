package com.moviebooking.theatre.service;

import com.moviebooking.theatre.exception.ResourceNotFoundException;
import com.moviebooking.theatre.model.*;
import com.moviebooking.theatre.repository.CityRepository;
import com.moviebooking.theatre.repository.TheatreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TheatreServiceTest {

    @Mock
    private TheatreRepository theatreRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private CityService cityService;

    @InjectMocks
    private TheatreService theatreService;

    private Theatre testTheatre;
    private City testCity;
    private TheatreRequest theatreRequest;
    private CityResponse cityResponse;

    @BeforeEach
    void setUp() {
        testCity = new City();
        testCity.setId(1L);
        testCity.setName("Mumbai");
        testCity.setState("Maharashtra");
        testCity.setCountry("India");
        testCity.setZipCode("400001");

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

        theatreRequest = new TheatreRequest();
        theatreRequest.setName("PVR Cinemas");
        theatreRequest.setAddress("123 Main Street");
        theatreRequest.setPhoneNumber("1234567890");
        theatreRequest.setEmail("pvr@example.com");
        theatreRequest.setLatitude(19.0760);
        theatreRequest.setLongitude(72.8777);
        theatreRequest.setCityId(1L);

        cityResponse = new CityResponse();
        cityResponse.setId(1L);
        cityResponse.setName("Mumbai");
        cityResponse.setState("Maharashtra");
        cityResponse.setCountry("India");
        cityResponse.setZipCode("400001");
    }

    @Test
    void createTheatre_ShouldCreateTheatreSuccessfully() {
        // Given
        when(cityRepository.findById(1L)).thenReturn(Optional.of(testCity));
        when(theatreRepository.save(any(Theatre.class))).thenReturn(testTheatre);
        when(cityService.getCityById(1L)).thenReturn(cityResponse);

        // When
        TheatreResponse result = theatreService.createTheatre(theatreRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("PVR Cinemas");
        assertThat(result.getAddress()).isEqualTo("123 Main Street");
        assertThat(result.getLatitude()).isEqualTo(19.0760);
        assertThat(result.getLongitude()).isEqualTo(72.8777);

        verify(cityRepository).findById(1L);
        verify(theatreRepository).save(any(Theatre.class));
        verify(outboxEventService).publishTheatreEvent("THEATRE_CREATED", testTheatre);
    }

    @Test
    void createTheatre_ShouldThrowException_WhenCityNotFound() {
        // Given
        when(cityRepository.findById(1L)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> theatreService.createTheatre(theatreRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("City not found with ID: 1");

        verify(theatreRepository, never()).save(any(Theatre.class));
        verify(outboxEventService, never()).publishTheatreEvent(anyString(), any(Theatre.class));
    }

    @Test
    void updateTheatre_ShouldUpdateTheatreSuccessfully() {
        // Given
        when(theatreRepository.findById(1L)).thenReturn(Optional.of(testTheatre));
        when(cityRepository.findById(1L)).thenReturn(Optional.of(testCity));
        when(theatreRepository.save(any(Theatre.class))).thenReturn(testTheatre);
        when(cityService.getCityById(1L)).thenReturn(cityResponse);

        theatreRequest.setName("Updated Theatre");

        // When
        TheatreResponse result = theatreService.updateTheatre(1L, theatreRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(testTheatre.getName()).isEqualTo("Updated Theatre");

        verify(theatreRepository).findById(1L);
        verify(cityRepository).findById(1L);
        verify(theatreRepository).save(testTheatre);
        verify(outboxEventService).publishTheatreEvent("THEATRE_UPDATED", testTheatre);
    }

    @Test
    void updateTheatre_ShouldThrowException_WhenTheatreNotFound() {
        // Given
        when(theatreRepository.findById(1L)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> theatreService.updateTheatre(1L, theatreRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Theatre not found with ID: 1");

        verify(cityRepository, never()).findById(any());
        verify(theatreRepository, never()).save(any(Theatre.class));
        verify(outboxEventService, never()).publishTheatreEvent(anyString(), any(Theatre.class));
    }

    @Test
    void updateTheatre_ShouldThrowException_WhenCityNotFound() {
        // Given
        when(theatreRepository.findById(1L)).thenReturn(Optional.of(testTheatre));
        when(cityRepository.findById(1L)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> theatreService.updateTheatre(1L, theatreRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("City not found with ID: 1");

        verify(theatreRepository, never()).save(any(Theatre.class));
        verify(outboxEventService, never()).publishTheatreEvent(anyString(), any(Theatre.class));
    }

    @Test
    void getTheatreById_ShouldReturnTheatre_WhenExists() {
        // Given
        when(theatreRepository.findById(1L)).thenReturn(Optional.of(testTheatre));
        when(cityService.getCityById(1L)).thenReturn(cityResponse);

        // When
        TheatreResponse result = theatreService.getTheatreById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("PVR Cinemas");
        verify(theatreRepository).findById(1L);
    }

    @Test
    void getTheatreById_ShouldThrowException_WhenNotFound() {
        // Given
        when(theatreRepository.findById(1L)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> theatreService.getTheatreById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Theatre not found with ID: 1");
    }

    @Test
    void getAllTheatres_ShouldReturnAllTheatres() {
        // Given
        Theatre theatre2 = new Theatre();
        theatre2.setId(2L);
        theatre2.setName("INOX");
        theatre2.setCity(testCity);

        when(theatreRepository.findAll()).thenReturn(List.of(testTheatre, theatre2));
        when(cityService.getCityById(1L)).thenReturn(cityResponse);

        // When
        List<TheatreResponse> result = theatreService.getAllTheatres();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("PVR Cinemas");
        assertThat(result.get(1).getName()).isEqualTo("INOX");
        verify(theatreRepository).findAll();
    }

    @Test
    void getTheatresByCity_ShouldReturnTheatresInCity() {
        // Given
        when(theatreRepository.findByCityId(1L)).thenReturn(List.of(testTheatre));
        when(cityService.getCityById(1L)).thenReturn(cityResponse);

        // When
        List<TheatreResponse> result = theatreService.getTheatresByCity(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("PVR Cinemas");
        verify(theatreRepository).findByCityId(1L);
    }

    @Test
    void searchTheatresByName_ShouldReturnMatchingTheatres() {
        // Given
        String searchName = "PVR";
        when(theatreRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(List.of(testTheatre));
        when(cityService.getCityById(1L)).thenReturn(cityResponse);

        // When
        List<TheatreResponse> result = theatreService.searchTheatresByName(searchName);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("PVR Cinemas");
        verify(theatreRepository).findByNameContainingIgnoreCase(searchName);
    }

    @Test
    void searchTheatresByCityAndName_ShouldReturnMatchingTheatres() {
        // Given
        String searchName = "PVR";
        when(theatreRepository.findByCityIdAndNameContainingIgnoreCase(1L, searchName)).thenReturn(List.of(testTheatre));
        when(cityService.getCityById(1L)).thenReturn(cityResponse);

        // When
        List<TheatreResponse> result = theatreService.searchTheatresByCityAndName(1L, searchName);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("PVR Cinemas");
        verify(theatreRepository).findByCityIdAndNameContainingIgnoreCase(1L, searchName);
    }

    @Test
    void getTheatresNearLocation_ShouldReturnNearbyTheatres() {
        // Given
        Double latitude = 19.0760;
        Double longitude = 72.8777;
        Double radiusKm = 5.0;
        Double radiusSquared = (radiusKm / 111.0) * (radiusKm / 111.0);

        when(theatreRepository.findTheatresWithinRadius(latitude, longitude, radiusSquared))
                .thenReturn(List.of(testTheatre));
        when(cityService.getCityById(1L)).thenReturn(cityResponse);

        // When
        List<TheatreResponse> result = theatreService.getTheatresNearLocation(latitude, longitude, radiusKm);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("PVR Cinemas");
        verify(theatreRepository).findTheatresWithinRadius(latitude, longitude, radiusSquared);
    }

    @Test
    void deleteTheatre_ShouldDeleteTheatreSuccessfully() {
        // Given
        when(theatreRepository.findById(1L)).thenReturn(Optional.of(testTheatre));

        // When
        theatreService.deleteTheatre(1L);

        // Then
        verify(theatreRepository).findById(1L);
        verify(theatreRepository).delete(testTheatre);
        verify(outboxEventService).publishTheatreEvent("THEATRE_DELETED", testTheatre);
    }

    @Test
    void deleteTheatre_ShouldThrowException_WhenTheatreNotFound() {
        // Given
        when(theatreRepository.findById(1L)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> theatreService.deleteTheatre(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Theatre not found with ID: 1");

        verify(theatreRepository, never()).delete(any(Theatre.class));
        verify(outboxEventService, never()).publishTheatreEvent(anyString(), any(Theatre.class));
    }

    @Test
    void convertToBigDecimal_ShouldHandleNullValue() {
        // When creating theatre with null coordinates
        theatreRequest.setLatitude(null);
        theatreRequest.setLongitude(null);

        when(cityRepository.findById(1L)).thenReturn(Optional.of(testCity));
        when(theatreRepository.save(any(Theatre.class))).thenAnswer(invocation -> {
            Theatre savedTheatre = invocation.getArgument(0);
            assertThat(savedTheatre.getLatitude()).isNull();
            assertThat(savedTheatre.getLongitude()).isNull();
            return testTheatre;
        });
        when(cityService.getCityById(1L)).thenReturn(cityResponse);

        // When
        TheatreResponse result = theatreService.createTheatre(theatreRequest);

        // Then
        verify(theatreRepository).save(any(Theatre.class));
    }

    @Test
    void convertToDouble_ShouldHandleNullBigDecimal() {
        // Given
        testTheatre.setLatitude(null);
        testTheatre.setLongitude(null);

        when(theatreRepository.findById(1L)).thenReturn(Optional.of(testTheatre));
        when(cityService.getCityById(1L)).thenReturn(cityResponse);

        // When
        TheatreResponse result = theatreService.getTheatreById(1L);

        // Then
        assertThat(result.getLatitude()).isNull();
        assertThat(result.getLongitude()).isNull();
    }
}