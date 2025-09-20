package com.moviebooking.search.service;

import com.moviebooking.search.model.CityDocument;
import com.moviebooking.search.model.ShowDocument;
import com.moviebooking.search.model.TheatreDocument;
import com.moviebooking.search.repository.CitySearchRepository;
import com.moviebooking.search.repository.ShowSearchRepository;
import com.moviebooking.search.repository.TheatreSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.convert.ConversionException;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private CitySearchRepository citySearchRepository;

    @Mock
    private TheatreSearchRepository theatreSearchRepository;

    @Mock
    private ShowSearchRepository showSearchRepository;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private IndexOperations indexOperations;

    @Mock
    private SearchHits<TheatreDocument> theatreSearchHits;

    @Mock
    private SearchHits<ShowDocument> showSearchHits;

    @Mock
    private SearchHit<TheatreDocument> theatreSearchHit;

    @Mock
    private SearchHit<ShowDocument> showSearchHit;

    @InjectMocks
    private SearchService searchService;

    private CityDocument testCity;
    private TheatreDocument testTheatre;
    private ShowDocument testShow;

    @BeforeEach
    void setUp() {
        testCity = new CityDocument();
        testCity.setId("1");
        testCity.setName("Mumbai");
        testCity.setState("Maharashtra");
        testCity.setCountry("India");
        testCity.setZipCode("400001");
        testCity.setCreatedAt(LocalDateTime.now());
        testCity.setUpdatedAt(LocalDateTime.now());

        testTheatre = new TheatreDocument();
        testTheatre.setId("1");
        testTheatre.setTheatreId(1L);
        testTheatre.setName("PVR Cinemas");
        testTheatre.setAddress("123 Main Street");
        testTheatre.setPhoneNumber("1234567890");
        testTheatre.setEmail("pvr@example.com");
        testTheatre.setLocation(new GeoPoint(19.0760, 72.8777));
        testTheatre.setCityId(1L);
        testTheatre.setCityName("Mumbai");
        testTheatre.setCreatedAt(LocalDateTime.now());
        testTheatre.setUpdatedAt(LocalDateTime.now());

        testShow = new ShowDocument();
        testShow.setId("1");
        testShow.setMovieId(1L);
        testShow.setMovieTitle("Avengers");
        testShow.setShowDateTime(LocalDateTime.now().plusDays(1));
        testShow.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(3));
        testShow.setPrice(BigDecimal.valueOf(250.00));
        testShow.setAvailableSeats(95);
        testShow.setStatus("ACTIVE");
        testShow.setScreenId(1L);
        testShow.setScreenName("Screen 1");
        testShow.setTheatreId(1L);
        testShow.setTheatreName("PVR Cinemas");
        testShow.setCityId(1L);
        testShow.setCityName("Mumbai");
        testShow.setCreatedAt(LocalDateTime.now());
        testShow.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void initializeIndices_ShouldCreateIndicesWhenNotExist() {
        // Given
        when(elasticsearchOperations.indexOps(CityDocument.class)).thenReturn(indexOperations);
        when(elasticsearchOperations.indexOps(TheatreDocument.class)).thenReturn(indexOperations);
        when(elasticsearchOperations.indexOps(ShowDocument.class)).thenReturn(indexOperations);
        when(indexOperations.exists()).thenReturn(false);
        when(indexOperations.create()).thenReturn(true);
        when(indexOperations.putMapping()).thenReturn(true);

        // When
        searchService.initializeIndices();

        // Then
        verify(indexOperations, times(3)).create();
        verify(indexOperations, times(3)).putMapping();
    }

    @Test
    void initializeIndices_ShouldNotCreateIndicesWhenTheyExist() {
        // Given
        when(elasticsearchOperations.indexOps(CityDocument.class)).thenReturn(indexOperations);
        when(elasticsearchOperations.indexOps(TheatreDocument.class)).thenReturn(indexOperations);
        when(elasticsearchOperations.indexOps(ShowDocument.class)).thenReturn(indexOperations);
        when(indexOperations.exists()).thenReturn(true);

        // When
        searchService.initializeIndices();

        // Then
        verify(indexOperations, never()).create();
        verify(indexOperations, never()).putMapping();
    }

    @Test
    void initializeIndices_ShouldHandleExceptionsGracefully() {
        // Given
        when(elasticsearchOperations.indexOps(CityDocument.class)).thenThrow(new RuntimeException("Connection failed"));

        // When & Then - Should not throw exception
        searchService.initializeIndices();

        // Should handle gracefully and not crash application
    }

    @Test
    void searchCities_ShouldReturnAllCities_WhenQueryIsNull() {
        // Given
        List<CityDocument> cities = List.of(testCity);
        when(citySearchRepository.findAll()).thenReturn(cities);

        // When
        List<CityDocument> result = searchService.searchCities(null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Mumbai");
        verify(citySearchRepository).findAll();
        verify(citySearchRepository, never()).findByNameContainingIgnoreCase(anyString());
    }

    @Test
    void searchCities_ShouldReturnAllCities_WhenQueryIsEmpty() {
        // Given
        List<CityDocument> cities = List.of(testCity);
        when(citySearchRepository.findAll()).thenReturn(cities);

        // When
        List<CityDocument> result = searchService.searchCities("");

        // Then
        assertThat(result).hasSize(1);
        verify(citySearchRepository).findAll();
    }

    @Test
    void searchCities_ShouldReturnMatchingCities_WhenQueryProvided() {
        // Given
        String query = "Mumbai";
        List<CityDocument> cities = List.of(testCity);
        when(citySearchRepository.findByNameContainingIgnoreCase(query)).thenReturn(cities);

        // When
        List<CityDocument> result = searchService.searchCities(query);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Mumbai");
        verify(citySearchRepository).findByNameContainingIgnoreCase(query);
    }

    @Test
    void searchCities_ShouldReturnEmptyList_WhenConversionExceptionOccurs() {
        // Given
        when(citySearchRepository.findByNameContainingIgnoreCase("Mumbai"))
                .thenThrow(new ConversionException("Conversion failed"));

        // When
        List<CityDocument> result = searchService.searchCities("Mumbai");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void searchCities_ShouldReturnEmptyList_WhenGeneralExceptionOccurs() {
        // Given
        when(citySearchRepository.findByNameContainingIgnoreCase("Mumbai"))
                .thenThrow(new RuntimeException("Database error"));

        // When
        List<CityDocument> result = searchService.searchCities("Mumbai");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void searchCitiesByState_ShouldReturnMatchingCities() {
        // Given
        String state = "Maharashtra";
        List<CityDocument> cities = List.of(testCity);
        when(citySearchRepository.findByStateIgnoreCase(state)).thenReturn(cities);

        // When
        List<CityDocument> result = searchService.searchCitiesByState(state);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getState()).isEqualTo("Maharashtra");
        verify(citySearchRepository).findByStateIgnoreCase(state);
    }

    @Test
    void searchCitiesByCountry_ShouldReturnMatchingCities() {
        // Given
        String country = "India";
        List<CityDocument> cities = List.of(testCity);
        when(citySearchRepository.findByCountryIgnoreCase(country)).thenReturn(cities);

        // When
        List<CityDocument> result = searchService.searchCitiesByCountry(country);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCountry()).isEqualTo("India");
        verify(citySearchRepository).findByCountryIgnoreCase(country);
    }

    @Test
    void searchTheatres_ShouldReturnAllTheatres_WhenQueryIsNull() {
        // Given
        List<TheatreDocument> theatres = List.of(testTheatre);
        when(theatreSearchRepository.findAll()).thenReturn(theatres);

        // When
        List<TheatreDocument> result = searchService.searchTheatres(null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("PVR Cinemas");
        verify(theatreSearchRepository).findAll();
    }

    @Test
    void searchTheatres_ShouldReturnMatchingTheatres_WhenQueryProvided() {
        // Given
        String query = "PVR";
        List<TheatreDocument> theatres = List.of(testTheatre);
        when(theatreSearchRepository.findByNameContainingIgnoreCase(query)).thenReturn(theatres);

        // When
        List<TheatreDocument> result = searchService.searchTheatres(query);

        // Then
        assertThat(result).hasSize(1);
        verify(theatreSearchRepository).findByNameContainingIgnoreCase(query);
    }

    @Test
    void searchTheatresByCity_ShouldReturnTheatresInCity() {
        // Given
        Long cityId = 1L;
        List<TheatreDocument> theatres = List.of(testTheatre);
        when(theatreSearchRepository.findByCityId(cityId)).thenReturn(theatres);

        // When
        List<TheatreDocument> result = searchService.searchTheatresByCity(cityId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCityId()).isEqualTo(1L);
        verify(theatreSearchRepository).findByCityId(cityId);
    }

    @Test
    void searchTheatreById_ShouldReturnTheatreWithMatchingId() {
        // Given
        Long theatreId = 1L;
        testTheatre.setTheatreId(theatreId);
        List<TheatreDocument> theatres = List.of(testTheatre);
        when(theatreSearchRepository.findByTheatreId(theatreId)).thenReturn(theatres);

        // When
        List<TheatreDocument> result = searchService.searchTheatreById(theatreId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTheatreId()).isEqualTo(theatreId);
        verify(theatreSearchRepository).findByTheatreId(theatreId);
    }

    @Test
    void searchTheatresNearLocation_ShouldReturnNearbyTheatres() {
        // Given
        double latitude = 19.0760;
        double longitude = 72.8777;
        String distance = "5km";

        when(elasticsearchOperations.search(any(Query.class), eq(TheatreDocument.class)))
                .thenReturn(theatreSearchHits);
        when(theatreSearchHits.stream()).thenReturn(Stream.of(theatreSearchHit));
        when(theatreSearchHit.getContent()).thenReturn(testTheatre);

        // When
        List<TheatreDocument> result = searchService.searchTheatresNearLocation(latitude, longitude, distance);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("PVR Cinemas");
        verify(elasticsearchOperations).search(any(Query.class), eq(TheatreDocument.class));
    }

    @Test
    void searchTheatresNearLocation_ShouldReturnEmptyList_WhenExceptionOccurs() {
        // Given
        when(elasticsearchOperations.search(any(Query.class), eq(TheatreDocument.class)))
                .thenThrow(new RuntimeException("Elasticsearch error"));

        // When
        List<TheatreDocument> result = searchService.searchTheatresNearLocation(19.0760, 72.8777, "5km");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void searchShows_ShouldReturnAllShows_WhenQueryIsNull() {
        // Given
        List<ShowDocument> shows = List.of(testShow);
        when(showSearchRepository.findAll()).thenReturn(shows);

        // When
        List<ShowDocument> result = searchService.searchShows(null);

        // Then
        assertThat(result).hasSize(1);
        verify(showSearchRepository).findAll();
    }

    @Test
    void searchShows_ShouldReturnMatchingShows_WhenQueryProvided() {
        // Given
        String query = "Avengers";
        List<ShowDocument> shows = List.of(testShow);
        when(showSearchRepository.findByMovieTitleContainingIgnoreCase(query)).thenReturn(shows);

        // When
        List<ShowDocument> result = searchService.searchShows(query);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMovieTitle()).isEqualTo("Avengers");
        verify(showSearchRepository).findByMovieTitleContainingIgnoreCase(query);
    }

    @Test
    void searchShowsByMovieAndCity_ShouldReturnMatchingShows() {
        // Given
        Long movieId = 1L;
        Long cityId = 1L;
        List<ShowDocument> shows = List.of(testShow);
        when(showSearchRepository.findByMovieIdAndCityId(movieId, cityId)).thenReturn(shows);

        // When
        List<ShowDocument> result = searchService.searchShowsByMovieAndCity(movieId, cityId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMovieId()).isEqualTo(1L);
        assertThat(result.get(0).getCityId()).isEqualTo(1L);
        verify(showSearchRepository).findByMovieIdAndCityId(movieId, cityId);
    }

    @Test
    void searchShowsWithFilters_ShouldReturnFilteredShows() {
        // Given
        String movieTitle = "Avengers";
        String cityName = "Mumbai";
        String theatreName = "PVR";
        LocalDateTime fromDateTime = LocalDateTime.now();
        LocalDateTime toDateTime = LocalDateTime.now().plusDays(7);

        when(elasticsearchOperations.search(any(Query.class), eq(ShowDocument.class)))
                .thenReturn(showSearchHits);
        when(showSearchHits.stream()).thenReturn(Stream.of(showSearchHit));
        when(showSearchHit.getContent()).thenReturn(testShow);

        // When
        List<ShowDocument> result = searchService.searchShowsWithFilters(
                movieTitle, cityName, theatreName, fromDateTime, toDateTime);

        // Then
        assertThat(result).hasSize(1);
        verify(elasticsearchOperations).search(any(Query.class), eq(ShowDocument.class));
    }

    @Test
    void searchShowsWithFilters_ShouldReturnEmptyList_WhenExceptionOccurs() {
        // Given
        when(elasticsearchOperations.search(any(Query.class), eq(ShowDocument.class)))
                .thenThrow(new RuntimeException("Query failed"));

        // When
        List<ShowDocument> result = searchService.searchShowsWithFilters(
                "Avengers", "Mumbai", "PVR", LocalDateTime.now(), LocalDateTime.now().plusDays(1));

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void isElasticsearchHealthy_ShouldReturnTrue_WhenAtLeastOneIndexExists() {
        // Given
        when(elasticsearchOperations.indexOps(CityDocument.class)).thenReturn(indexOperations);
        when(elasticsearchOperations.indexOps(TheatreDocument.class)).thenReturn(indexOperations);
        lenient().when(elasticsearchOperations.indexOps(ShowDocument.class)).thenReturn(indexOperations);
        when(indexOperations.exists())
                .thenReturn(false) // City index doesn't exist
                .thenReturn(true)  // Theatre index exists
                .thenReturn(false); // Show index doesn't exist

        // When
        boolean result = searchService.isElasticsearchHealthy();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isElasticsearchHealthy_ShouldReturnFalse_WhenNoIndicesExist() {
        // Given
        when(elasticsearchOperations.indexOps(CityDocument.class)).thenReturn(indexOperations);
        when(elasticsearchOperations.indexOps(TheatreDocument.class)).thenReturn(indexOperations);
        when(elasticsearchOperations.indexOps(ShowDocument.class)).thenReturn(indexOperations);
        when(indexOperations.exists()).thenReturn(false);

        // When
        boolean result = searchService.isElasticsearchHealthy();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isElasticsearchHealthy_ShouldReturnFalse_WhenExceptionOccurs() {
        // Given
        when(elasticsearchOperations.indexOps(CityDocument.class))
                .thenThrow(new RuntimeException("Connection failed"));

        // When
        boolean result = searchService.isElasticsearchHealthy();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void recreateIndices_ShouldDeleteAndCreateAllIndices() {
        // Given
        when(elasticsearchOperations.indexOps(CityDocument.class)).thenReturn(indexOperations);
        when(elasticsearchOperations.indexOps(TheatreDocument.class)).thenReturn(indexOperations);
        when(elasticsearchOperations.indexOps(ShowDocument.class)).thenReturn(indexOperations);
        when(indexOperations.exists()).thenReturn(true);
        when(indexOperations.delete()).thenReturn(true);
        when(indexOperations.create()).thenReturn(true);
        when(indexOperations.putMapping()).thenReturn(true);

        // When
        searchService.recreateIndices();

        // Then
        verify(indexOperations, times(3)).delete();
        verify(indexOperations, times(3)).create();
        verify(indexOperations, times(3)).putMapping();
    }

    @Test
    void recreateIndices_ShouldThrowException_WhenOperationFails() {
        // Given
        when(elasticsearchOperations.indexOps(CityDocument.class)).thenReturn(indexOperations);
        when(indexOperations.exists()).thenReturn(true);
        when(indexOperations.delete()).thenThrow(new RuntimeException("Delete failed"));

        // Then
        assertThatThrownBy(() -> searchService.recreateIndices())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to recreate indices");
    }
}