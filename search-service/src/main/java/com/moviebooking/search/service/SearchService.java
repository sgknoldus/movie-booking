package com.moviebooking.search.service;

import com.moviebooking.search.model.CityDocument;
import com.moviebooking.search.model.ShowDocument;
import com.moviebooking.search.model.TheatreDocument;
import com.moviebooking.search.repository.CitySearchRepository;
import com.moviebooking.search.repository.ShowSearchRepository;
import com.moviebooking.search.repository.TheatreSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final CitySearchRepository citySearchRepository;
    private final TheatreSearchRepository theatreSearchRepository;
    private final ShowSearchRepository showSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    
    @PostConstruct
    public void initializeIndices() {
        try {
            log.info("Checking Elasticsearch indices on startup...");
            
            // Only create indices if they don't exist, don't delete existing data
            if (!elasticsearchOperations.indexOps(CityDocument.class).exists()) {
                elasticsearchOperations.indexOps(CityDocument.class).create();
                elasticsearchOperations.indexOps(CityDocument.class).putMapping();
                log.info("Created cities index with mappings");
            }
            
            if (!elasticsearchOperations.indexOps(TheatreDocument.class).exists()) {
                elasticsearchOperations.indexOps(TheatreDocument.class).create();
                elasticsearchOperations.indexOps(TheatreDocument.class).putMapping();
                log.info("Created theatres index with mappings");
            }
            
            if (!elasticsearchOperations.indexOps(ShowDocument.class).exists()) {
                elasticsearchOperations.indexOps(ShowDocument.class).create();
                elasticsearchOperations.indexOps(ShowDocument.class).putMapping();
                log.info("Created shows index with mappings");
            }
            
            log.info("Elasticsearch indices check completed");
        } catch (Exception e) {
            log.error("Failed to initialize Elasticsearch indices: {}", e.getMessage(), e);
            // Don't throw here to allow application to start, but log the error
        }
    }
    
    // City Search Methods
    public List<CityDocument> searchCities(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return StreamSupport.stream(citySearchRepository.findAll().spliterator(), false)
                        .collect(Collectors.toList());
            }
            return citySearchRepository.findByNameContainingIgnoreCase(query);
        } catch (org.springframework.data.elasticsearch.core.convert.ConversionException e) {
            log.error("Elasticsearch conversion error for cities search, corrupted data detected: {}", e.getMessage());
            log.warn("To fix this issue, call POST /api/v1/search/cities/admin/recreate-indices to recreate indices with correct mappings");
            return List.of();
        } catch (Exception e) {
            log.error("Failed to search cities: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    public List<CityDocument> searchCitiesByState(String state) {
        try {
            return citySearchRepository.findByStateIgnoreCase(state);
        } catch (org.springframework.data.elasticsearch.core.convert.ConversionException e) {
            log.error("Elasticsearch conversion error for cities by state search, corrupted data detected: {}", e.getMessage());
            log.warn("To fix this issue, call POST /api/v1/search/cities/admin/recreate-indices to recreate indices with correct mappings");
            return List.of();
        } catch (Exception e) {
            log.error("Failed to search cities by state: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    public List<CityDocument> searchCitiesByCountry(String country) {
        try {
            return citySearchRepository.findByCountryIgnoreCase(country);
        } catch (org.springframework.data.elasticsearch.core.convert.ConversionException e) {
            log.error("Elasticsearch conversion error for cities by country search, corrupted data detected: {}", e.getMessage());
            log.warn("To fix this issue, call POST /api/v1/search/cities/admin/recreate-indices to recreate indices with correct mappings");
            return List.of();
        } catch (Exception e) {
            log.error("Failed to search cities by country: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    // Theatre Search Methods
    public List<TheatreDocument> searchTheatres(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return StreamSupport.stream(theatreSearchRepository.findAll().spliterator(), false)
                        .collect(Collectors.toList());
            }
            return theatreSearchRepository.findByNameContainingIgnoreCase(query);
        } catch (Exception e) {
            log.error("Failed to search theatres: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    public List<TheatreDocument> searchTheatresByCity(Long cityId) {
        try {
            return theatreSearchRepository.findByCityId(cityId);
        } catch (Exception e) {
            log.error("Failed to search theatres by city {}: {}", cityId, e.getMessage(), e);
            return List.of();
        }
    }

    public List<TheatreDocument> searchTheatresByCityName(String cityName) {
        try {
            return theatreSearchRepository.findByCityNameContainingIgnoreCase(cityName);
        } catch (Exception e) {
            log.error("Failed to search theatres by city name '{}': {}", cityName, e.getMessage(), e);
            return List.of();
        }
    }

    public List<TheatreDocument> searchTheatresByAddress(String address) {
        try {
            return theatreSearchRepository.findByAddressContainingIgnoreCase(address);
        } catch (Exception e) {
            log.error("Failed to search theatres by address '{}': {}", address, e.getMessage(), e);
            return List.of();
        }
    }
    
    public List<TheatreDocument> searchTheatresNearLocation(double latitude, double longitude, String distance) {
        try {
            Criteria criteria = new Criteria("location")
                    .within(new GeoPoint(latitude, longitude), distance);
            Query searchQuery = new CriteriaQuery(criteria);
            
            SearchHits<TheatreDocument> searchHits = elasticsearchOperations.search(searchQuery, TheatreDocument.class);
            return searchHits.stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to search theatres near location: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    // Show Search Methods
    public List<ShowDocument> searchShows(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return StreamSupport.stream(showSearchRepository.findAll().spliterator(), false)
                        .collect(Collectors.toList());
            }
            return showSearchRepository.findByMovieTitleContainingIgnoreCase(query);
        } catch (Exception e) {
            log.error("Failed to search shows: {}", e.getMessage(), e);
            return List.of();
        }
    }

    public List<ShowDocument> searchShowsByMovie(Long movieId) {
        try {
            log.debug("Searching shows for movie ID: {}", movieId);
            List<ShowDocument> results = showSearchRepository.findByMovieId(movieId);
            log.debug("Found {} shows for movie ID: {}", results.size(), movieId);
            return results;
        } catch (Exception e) {
            log.error("Failed to search shows by movie {}: {}", movieId, e.getMessage(), e);
            return List.of();
        }
    }

    public List<ShowDocument> searchShowsByTheatre(Long theatreId) {
        try {
            log.debug("Searching shows for theatre ID: {}", theatreId);
            List<ShowDocument> results = showSearchRepository.findByTheatreId(theatreId);
            log.debug("Found {} shows for theatre ID: {}", results.size(), theatreId);
            return results;
        } catch (Exception e) {
            log.error("Failed to search shows by theatre {}: {}", theatreId, e.getMessage(), e);
            return List.of();
        }
    }

    public List<ShowDocument> searchShowsByCity(Long cityId) {
        try {
            log.debug("Searching shows for city ID: {}", cityId);
            List<ShowDocument> results = showSearchRepository.findByCityId(cityId);
            log.debug("Found {} shows for city ID: {}", results.size(), cityId);
            return results;
        } catch (Exception e) {
            log.error("Failed to search shows by city {}: {}", cityId, e.getMessage(), e);
            return List.of();
        }
    }

    public List<ShowDocument> searchShowsByMovieAndCity(Long movieId, Long cityId) {
        try {
            return showSearchRepository.findByMovieIdAndCityId(movieId, cityId);
        } catch (Exception e) {
            log.error("Failed to search shows by movie {} and city {}: {}", movieId, cityId, e.getMessage(), e);
            return List.of();
        }
    }

    public List<ShowDocument> searchShowsByMovieAndCityAfterDateTime(Long movieId, Long cityId, LocalDateTime dateTime) {
        try {
            return showSearchRepository.findByMovieIdAndCityIdAndShowDateTimeAfter(movieId, cityId, dateTime);
        } catch (Exception e) {
            log.error("Failed to search shows by movie {}, city {} after {}: {}", movieId, cityId, dateTime, e.getMessage(), e);
            return List.of();
        }
    }
    
    public List<ShowDocument> searchShowsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            return showSearchRepository.findByShowDateTimeBetween(startDate, endDate);
        } catch (Exception e) {
            log.error("Failed to search shows by date range {} to {}: {}", startDate, endDate, e.getMessage(), e);
            return List.of();
        }
    }

    public List<ShowDocument> searchShowsByTheatreName(String theatreName) {
        try {
            return showSearchRepository.findByTheatreNameContainingIgnoreCase(theatreName);
        } catch (Exception e) {
            log.error("Failed to search shows by theatre name '{}': {}", theatreName, e.getMessage(), e);
            return List.of();
        }
    }

    public List<ShowDocument> searchShowsByCityName(String cityName) {
        try {
            return showSearchRepository.findByCityNameContainingIgnoreCase(cityName);
        } catch (Exception e) {
            log.error("Failed to search shows by city name '{}': {}", cityName, e.getMessage(), e);
            return List.of();
        }
    }
    
    // Advanced Search Methods
    public List<ShowDocument> searchShowsWithFilters(String movieTitle, String cityName, 
                                                    String theatreName, LocalDateTime fromDateTime, 
                                                    LocalDateTime toDateTime) {
        try {
            Criteria criteria = new Criteria();
            
            if (movieTitle != null && !movieTitle.trim().isEmpty()) {
                criteria = criteria.and(new Criteria("movieTitle").contains(movieTitle));
            }
            
            if (cityName != null && !cityName.trim().isEmpty()) {
                criteria = criteria.and(new Criteria("cityName").contains(cityName));
            }
            
            if (theatreName != null && !theatreName.trim().isEmpty()) {
                criteria = criteria.and(new Criteria("theatreName").contains(theatreName));
            }
            
            if (fromDateTime != null) {
                criteria = criteria.and(new Criteria("showDateTime").greaterThanEqual(fromDateTime));
            }
            
            if (toDateTime != null) {
                criteria = criteria.and(new Criteria("showDateTime").lessThanEqual(toDateTime));
            }
            
            Query searchQuery = new CriteriaQuery(criteria);
            SearchHits<ShowDocument> searchHits = elasticsearchOperations.search(searchQuery, ShowDocument.class);
            
            return searchHits.stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Failed to search shows with filters: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    // Health check method
    public boolean isElasticsearchHealthy() {
        try {
            return elasticsearchOperations.indexOps(CityDocument.class).exists() ||
                   elasticsearchOperations.indexOps(TheatreDocument.class).exists() ||
                   elasticsearchOperations.indexOps(ShowDocument.class).exists();
        } catch (Exception e) {
            log.error("Elasticsearch health check failed: {}", e.getMessage());
            return false;
        }
    }

    // Count methods for debugging
    public long countTheatres() {
        try {
            return theatreSearchRepository.count();
        } catch (Exception e) {
            log.error("Failed to count theatres: {}", e.getMessage(), e);
            return 0;
        }
    }

    public long countCities() {
        try {
            return citySearchRepository.count();
        } catch (Exception e) {
            log.error("Failed to count cities: {}", e.getMessage(), e);
            return 0;
        }
    }

    public long countShows() {
        try {
            return showSearchRepository.count();
        } catch (Exception e) {
            log.error("Failed to count shows: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    // Method to recreate all indices with correct mappings
    public void recreateIndices() {
        try {
            log.info("Starting to recreate Elasticsearch indices...");
            
            // Delete existing indices
            if (elasticsearchOperations.indexOps(CityDocument.class).exists()) {
                elasticsearchOperations.indexOps(CityDocument.class).delete();
                log.info("Deleted cities index");
            }
            
            if (elasticsearchOperations.indexOps(TheatreDocument.class).exists()) {
                elasticsearchOperations.indexOps(TheatreDocument.class).delete();
                log.info("Deleted theatres index");
            }
            
            if (elasticsearchOperations.indexOps(ShowDocument.class).exists()) {
                elasticsearchOperations.indexOps(ShowDocument.class).delete();
                log.info("Deleted shows index");
            }
            
            // Create new indices with correct mappings
            elasticsearchOperations.indexOps(CityDocument.class).create();
            elasticsearchOperations.indexOps(CityDocument.class).putMapping();
            log.info("Created cities index with new mappings");
            
            elasticsearchOperations.indexOps(TheatreDocument.class).create();
            elasticsearchOperations.indexOps(TheatreDocument.class).putMapping();
            log.info("Created theatres index with new mappings");
            
            elasticsearchOperations.indexOps(ShowDocument.class).create();
            elasticsearchOperations.indexOps(ShowDocument.class).putMapping();
            log.info("Created shows index with new mappings");
            
            log.info("Successfully recreated all Elasticsearch indices");
            
        } catch (Exception e) {
            log.error("Failed to recreate Elasticsearch indices: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to recreate indices", e);
        }
    }
}