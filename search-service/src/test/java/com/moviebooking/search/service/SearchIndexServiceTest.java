package com.moviebooking.search.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebooking.search.model.CityDocument;
import com.moviebooking.search.model.ShowDocument;
import com.moviebooking.search.model.TheatreDocument;
import com.moviebooking.search.repository.CitySearchRepository;
import com.moviebooking.search.repository.ShowSearchRepository;
import com.moviebooking.search.repository.TheatreSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchIndexServiceTest {

    @Mock
    private CitySearchRepository citySearchRepository;

    @Mock
    private TheatreSearchRepository theatreSearchRepository;

    @Mock
    private ShowSearchRepository showSearchRepository;

    @InjectMocks
    private SearchIndexService searchIndexService;

    private ObjectMapper objectMapper;
    private JsonNode cityJsonNode;
    private JsonNode theatreJsonNode;
    private JsonNode showJsonNode;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();

        // Setup city JSON data
        String cityJson = """
                {
                    "id": "1",
                    "name": "Mumbai",
                    "state": "Maharashtra",
                    "country": "India",
                    "zipCode": "400001",
                    "createdAt": "2023-09-11T10:30:00",
                    "updatedAt": "2023-09-11T10:30:00"
                }
                """;
        cityJsonNode = objectMapper.readTree(cityJson);

        // Setup theatre JSON data
        String theatreJson = """
                {
                    "id": "1",
                    "name": "PVR Cinemas",
                    "address": "123 Main Street",
                    "phoneNumber": "1234567890",
                    "email": "pvr@example.com",
                    "latitude": 19.0760,
                    "longitude": 72.8777,
                    "cityId": 1,
                    "cityName": "Mumbai",
                    "createdAt": "2023-09-11T10:30:00",
                    "updatedAt": "2023-09-11T10:30:00"
                }
                """;
        theatreJsonNode = objectMapper.readTree(theatreJson);

        // Setup show JSON data
        String showJson = """
                {
                    "id": "1",
                    "movieId": 1,
                    "movieTitle": "Avengers",
                    "showDateTime": "2023-09-12T19:00:00",
                    "endDateTime": "2023-09-12T22:00:00",
                    "price": "250.00",
                    "availableSeats": 95,
                    "status": "ACTIVE",
                    "screenId": 1,
                    "screenName": "Screen 1",
                    "theatreId": 1,
                    "theatreName": "PVR Cinemas",
                    "cityId": 1,
                    "cityName": "Mumbai",
                    "createdAt": "2023-09-11T10:30:00",
                    "updatedAt": "2023-09-11T10:30:00"
                }
                """;
        showJsonNode = objectMapper.readTree(showJson);
    }

    @Test
    void indexCity_ShouldCreateAndSaveCityDocument() {
        // Given
        when(citySearchRepository.save(any(CityDocument.class))).thenReturn(new CityDocument());

        // When
        searchIndexService.indexCity(cityJsonNode);

        // Then
        ArgumentCaptor<CityDocument> cityCaptor = ArgumentCaptor.forClass(CityDocument.class);
        verify(citySearchRepository).save(cityCaptor.capture());

        CityDocument savedCity = cityCaptor.getValue();
        assertThat(savedCity.getId()).isEqualTo("1");
        assertThat(savedCity.getName()).isEqualTo("Mumbai");
        assertThat(savedCity.getState()).isEqualTo("Maharashtra");
        assertThat(savedCity.getCountry()).isEqualTo("India");
        assertThat(savedCity.getZipCode()).isEqualTo("400001");
        assertThat(savedCity.getCreatedAt()).isNotNull();
        assertThat(savedCity.getUpdatedAt()).isNotNull();
    }

    @Test
    void indexCity_ShouldHandleNullZipCode() throws Exception {
        // Given
        String cityJsonWithoutZip = """
                {
                    "id": "1",
                    "name": "Mumbai",
                    "state": "Maharashtra",
                    "country": "India",
                    "zipCode": null,
                    "createdAt": "2023-09-11T10:30:00",
                    "updatedAt": "2023-09-11T10:30:00"
                }
                """;
        JsonNode cityNode = objectMapper.readTree(cityJsonWithoutZip);
        when(citySearchRepository.save(any(CityDocument.class))).thenReturn(new CityDocument());

        // When
        searchIndexService.indexCity(cityNode);

        // Then
        ArgumentCaptor<CityDocument> cityCaptor = ArgumentCaptor.forClass(CityDocument.class);
        verify(citySearchRepository).save(cityCaptor.capture());

        CityDocument savedCity = cityCaptor.getValue();
        assertThat(savedCity.getZipCode()).isNull();
    }

    @Test
    void indexCity_ShouldHandleException() {
        // Given
        when(citySearchRepository.save(any(CityDocument.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then - Should not throw exception
        searchIndexService.indexCity(cityJsonNode);

        verify(citySearchRepository).save(any(CityDocument.class));
    }

    @Test
    void indexTheatre_ShouldCreateAndSaveTheatreDocument() {
        // Given
        when(theatreSearchRepository.save(any(TheatreDocument.class))).thenReturn(new TheatreDocument());

        // When
        searchIndexService.indexTheatre(theatreJsonNode);

        // Then
        ArgumentCaptor<TheatreDocument> theatreCaptor = ArgumentCaptor.forClass(TheatreDocument.class);
        verify(theatreSearchRepository).save(theatreCaptor.capture());

        TheatreDocument savedTheatre = theatreCaptor.getValue();
        assertThat(savedTheatre.getId()).isEqualTo("1");
        assertThat(savedTheatre.getName()).isEqualTo("PVR Cinemas");
        assertThat(savedTheatre.getAddress()).isEqualTo("123 Main Street");
        assertThat(savedTheatre.getPhoneNumber()).isEqualTo("1234567890");
        assertThat(savedTheatre.getEmail()).isEqualTo("pvr@example.com");
        assertThat(savedTheatre.getLocation()).isNotNull();
        assertThat(savedTheatre.getLocation().getLat()).isEqualTo(19.0760);
        assertThat(savedTheatre.getLocation().getLon()).isEqualTo(72.8777);
        assertThat(savedTheatre.getCityId()).isEqualTo(1L);
        assertThat(savedTheatre.getCityName()).isEqualTo("Mumbai");
    }

    @Test
    void indexTheatre_ShouldHandleNullOptionalFields() throws Exception {
        // Given
        String theatreJsonWithNulls = """
                {
                    "id": "1",
                    "name": "PVR Cinemas",
                    "address": "123 Main Street",
                    "phoneNumber": null,
                    "email": null,
                    "latitude": null,
                    "longitude": null,
                    "cityId": 1,
                    "cityName": "Mumbai",
                    "createdAt": "2023-09-11T10:30:00",
                    "updatedAt": "2023-09-11T10:30:00"
                }
                """;
        JsonNode theatreNode = objectMapper.readTree(theatreJsonWithNulls);
        when(theatreSearchRepository.save(any(TheatreDocument.class))).thenReturn(new TheatreDocument());

        // When
        searchIndexService.indexTheatre(theatreNode);

        // Then
        ArgumentCaptor<TheatreDocument> theatreCaptor = ArgumentCaptor.forClass(TheatreDocument.class);
        verify(theatreSearchRepository).save(theatreCaptor.capture());

        TheatreDocument savedTheatre = theatreCaptor.getValue();
        assertThat(savedTheatre.getPhoneNumber()).isNull();
        assertThat(savedTheatre.getEmail()).isNull();
        assertThat(savedTheatre.getLocation()).isNull();
    }

    @Test
    void indexShow_ShouldCreateAndSaveShowDocument() {
        // Given
        when(showSearchRepository.save(any(ShowDocument.class))).thenReturn(new ShowDocument());

        // When
        searchIndexService.indexShow(showJsonNode);

        // Then
        ArgumentCaptor<ShowDocument> showCaptor = ArgumentCaptor.forClass(ShowDocument.class);
        verify(showSearchRepository).save(showCaptor.capture());

        ShowDocument savedShow = showCaptor.getValue();
        assertThat(savedShow.getId()).isEqualTo("1");
        assertThat(savedShow.getMovieId()).isEqualTo(1L);
        assertThat(savedShow.getMovieTitle()).isEqualTo("Avengers");
        assertThat(savedShow.getPrice().compareTo(BigDecimal.valueOf(250.00))).isEqualTo(0);
        assertThat(savedShow.getAvailableSeats()).isEqualTo(95);
        assertThat(savedShow.getStatus()).isEqualTo("ACTIVE");
        assertThat(savedShow.getScreenId()).isEqualTo(1L);
        assertThat(savedShow.getTheatreId()).isEqualTo(1L);
        assertThat(savedShow.getCityId()).isEqualTo(1L);
        assertThat(savedShow.getShowDateTime()).isNotNull();
        assertThat(savedShow.getEndDateTime()).isNotNull();
    }

    @Test
    void deleteCity_ShouldCallRepositoryDelete() {
        // Given
        String cityId = "1";

        // When
        searchIndexService.deleteCity(cityId);

        // Then
        verify(citySearchRepository).deleteById(cityId);
    }

    @Test
    void deleteCity_ShouldHandleException() {
        // Given
        String cityId = "1";
        doThrow(new RuntimeException("Delete failed")).when(citySearchRepository).deleteById(cityId);

        // When & Then - Should not throw exception
        searchIndexService.deleteCity(cityId);

        verify(citySearchRepository).deleteById(cityId);
    }

    @Test
    void deleteTheatre_ShouldCallRepositoryDelete() {
        // Given
        String theatreId = "1";

        // When
        searchIndexService.deleteTheatre(theatreId);

        // Then
        verify(theatreSearchRepository).deleteById(theatreId);
    }

    @Test
    void deleteShow_ShouldCallRepositoryDelete() {
        // Given
        String showId = "1";

        // When
        searchIndexService.deleteShow(showId);

        // Then
        verify(showSearchRepository).deleteById(showId);
    }

    @Test
    void updateShowSeatAvailability_ShouldDecreaseAvailableSeats_WhenSeatsBooked() {
        // Given
        Long showId = 1L;
        List<String> seatNumbers = List.of("A1", "A2");
        ShowDocument existingShow = new ShowDocument();
        existingShow.setId("1");
        existingShow.setAvailableSeats(100);

        when(showSearchRepository.findById("1")).thenReturn(Optional.of(existingShow));
        when(showSearchRepository.save(any(ShowDocument.class))).thenReturn(existingShow);

        // When
        searchIndexService.updateShowSeatAvailability(showId, seatNumbers, false);

        // Then
        verify(showSearchRepository).findById("1");
        verify(showSearchRepository).save(existingShow);
        assertThat(existingShow.getAvailableSeats()).isEqualTo(98); // 100 - 2
        assertThat(existingShow.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateShowSeatAvailability_ShouldIncreaseAvailableSeats_WhenSeatsReleased() {
        // Given
        Long showId = 1L;
        List<String> seatNumbers = List.of("A1", "A2", "A3");
        ShowDocument existingShow = new ShowDocument();
        existingShow.setId("1");
        existingShow.setAvailableSeats(95);

        when(showSearchRepository.findById("1")).thenReturn(Optional.of(existingShow));
        when(showSearchRepository.save(any(ShowDocument.class))).thenReturn(existingShow);

        // When
        searchIndexService.updateShowSeatAvailability(showId, seatNumbers, true);

        // Then
        verify(showSearchRepository).findById("1");
        verify(showSearchRepository).save(existingShow);
        assertThat(existingShow.getAvailableSeats()).isEqualTo(98); // 95 + 3
    }

    @Test
    void updateShowSeatAvailability_ShouldNotGoBelowZero_WhenBookingMoreSeatsThanAvailable() {
        // Given
        Long showId = 1L;
        List<String> seatNumbers = List.of("A1", "A2", "A3");
        ShowDocument existingShow = new ShowDocument();
        existingShow.setId("1");
        existingShow.setAvailableSeats(2); // Less than seats being booked

        when(showSearchRepository.findById("1")).thenReturn(Optional.of(existingShow));
        when(showSearchRepository.save(any(ShowDocument.class))).thenReturn(existingShow);

        // When
        searchIndexService.updateShowSeatAvailability(showId, seatNumbers, false);

        // Then
        assertThat(existingShow.getAvailableSeats()).isEqualTo(0); // Should not go below 0
    }

    @Test
    void updateShowSeatAvailability_ShouldDoNothing_WhenShowNotFound() {
        // Given
        Long showId = 1L;
        List<String> seatNumbers = List.of("A1", "A2");

        when(showSearchRepository.findById("1")).thenReturn(Optional.empty());

        // When
        searchIndexService.updateShowSeatAvailability(showId, seatNumbers, false);

        // Then
        verify(showSearchRepository).findById("1");
        verify(showSearchRepository, never()).save(any(ShowDocument.class));
    }

    @Test
    void updateShowSeatAvailability_ShouldHandleException() {
        // Given
        Long showId = 1L;
        List<String> seatNumbers = List.of("A1");

        when(showSearchRepository.findById("1")).thenThrow(new RuntimeException("Database error"));

        // When & Then - Should not throw exception
        searchIndexService.updateShowSeatAvailability(showId, seatNumbers, false);

        verify(showSearchRepository).findById("1");
    }

    @Test
    void parseDateTime_ShouldParseISODateTime() throws Exception {
        // Given
        String dateTimeJson = """
                {
                    "id": "1",
                    "name": "Test City",
                    "state": "Test State",
                    "country": "Test Country",
                    "createdAt": "2023-09-11T14:30:00",
                    "updatedAt": "2023-09-11T14:30:00"
                }
                """;
        JsonNode node = objectMapper.readTree(dateTimeJson);
        when(citySearchRepository.save(any(CityDocument.class))).thenReturn(new CityDocument());

        // When
        searchIndexService.indexCity(node);

        // Then
        ArgumentCaptor<CityDocument> cityCaptor = ArgumentCaptor.forClass(CityDocument.class);
        verify(citySearchRepository).save(cityCaptor.capture());

        CityDocument savedCity = cityCaptor.getValue();
        assertThat(savedCity.getCreatedAt().getHour()).isEqualTo(14);
        assertThat(savedCity.getCreatedAt().getMinute()).isEqualTo(30);
    }

    @Test
    void parseDateTime_ShouldHandleDateOnlyFormat() throws Exception {
        // Given
        String dateOnlyJson = """
                {
                    "id": "1",
                    "name": "Test City",
                    "state": "Test State",
                    "country": "Test Country",
                    "createdAt": "2023-09-11",
                    "updatedAt": "2023-09-11"
                }
                """;
        JsonNode node = objectMapper.readTree(dateOnlyJson);
        when(citySearchRepository.save(any(CityDocument.class))).thenReturn(new CityDocument());

        // When
        searchIndexService.indexCity(node);

        // Then
        ArgumentCaptor<CityDocument> cityCaptor = ArgumentCaptor.forClass(CityDocument.class);
        verify(citySearchRepository).save(cityCaptor.capture());

        CityDocument savedCity = cityCaptor.getValue();
        assertThat(savedCity.getCreatedAt().toLocalDate().toString()).isEqualTo("2023-09-11");
        assertThat(savedCity.getCreatedAt().getHour()).isEqualTo(0);
        assertThat(savedCity.getCreatedAt().getMinute()).isEqualTo(0);
    }

    @Test
    void parseDateTime_ShouldHandleAlternativeFormat() throws Exception {
        // Given
        String alternativeFormatJson = """
                {
                    "id": "1",
                    "name": "Test City",
                    "state": "Test State",
                    "country": "Test Country",
                    "createdAt": "2023-09-11 14:30:00",
                    "updatedAt": "2023-09-11 14:30:00"
                }
                """;
        JsonNode node = objectMapper.readTree(alternativeFormatJson);
        when(citySearchRepository.save(any(CityDocument.class))).thenReturn(new CityDocument());

        // When
        searchIndexService.indexCity(node);

        // Then
        ArgumentCaptor<CityDocument> cityCaptor = ArgumentCaptor.forClass(CityDocument.class);
        verify(citySearchRepository).save(cityCaptor.capture());

        CityDocument savedCity = cityCaptor.getValue();
        assertThat(savedCity.getCreatedAt().getHour()).isEqualTo(14);
        assertThat(savedCity.getCreatedAt().getMinute()).isEqualTo(30);
    }

    @Test
    void parseDateTime_ShouldUseCurrentTime_WhenFormatInvalid() throws Exception {
        // Given
        LocalDateTime beforeTest = LocalDateTime.now();
        String invalidDateJson = """
                {
                    "id": "1",
                    "name": "Test City",
                    "state": "Test State",
                    "country": "Test Country",
                    "createdAt": "invalid-date-format",
                    "updatedAt": "invalid-date-format"
                }
                """;
        JsonNode node = objectMapper.readTree(invalidDateJson);
        when(citySearchRepository.save(any(CityDocument.class))).thenReturn(new CityDocument());

        // When
        searchIndexService.indexCity(node);

        // Then
        ArgumentCaptor<CityDocument> cityCaptor = ArgumentCaptor.forClass(CityDocument.class);
        verify(citySearchRepository).save(cityCaptor.capture());

        CityDocument savedCity = cityCaptor.getValue();
        assertThat(savedCity.getCreatedAt()).isAfter(beforeTest);
        assertThat(savedCity.getCreatedAt()).isBefore(LocalDateTime.now().plusMinutes(1));
    }

    @Test
    void parseDateTime_ShouldUseCurrentTime_WhenDateTimeIsNull() throws Exception {
        // Given
        LocalDateTime beforeTest = LocalDateTime.now();
        String nullDateJson = """
                {
                    "id": "1",
                    "name": "Test City",
                    "state": "Test State",
                    "country": "Test Country",
                    "createdAt": null,
                    "updatedAt": null
                }
                """;
        JsonNode node = objectMapper.readTree(nullDateJson);
        when(citySearchRepository.save(any(CityDocument.class))).thenReturn(new CityDocument());

        // When
        searchIndexService.indexCity(node);

        // Then
        ArgumentCaptor<CityDocument> cityCaptor = ArgumentCaptor.forClass(CityDocument.class);
        verify(citySearchRepository).save(cityCaptor.capture());

        CityDocument savedCity = cityCaptor.getValue();
        assertThat(savedCity.getCreatedAt()).isAfter(beforeTest);
        assertThat(savedCity.getUpdatedAt()).isAfter(beforeTest);
    }
}