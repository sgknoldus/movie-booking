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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final CitySearchRepository citySearchRepository;
    private final TheatreSearchRepository theatreSearchRepository;
    private final ShowSearchRepository showSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    
    // City Search Methods
    public List<CityDocument> searchCities(String query) {
        if (query == null || query.trim().isEmpty()) {
            return (List<CityDocument>) citySearchRepository.findAll();
        }
        return citySearchRepository.findByNameContainingIgnoreCase(query);
    }
    
    public List<CityDocument> searchCitiesByState(String state) {
        return citySearchRepository.findByStateIgnoreCase(state);
    }
    
    public List<CityDocument> searchCitiesByCountry(String country) {
        return citySearchRepository.findByCountryIgnoreCase(country);
    }
    
    // Theatre Search Methods
    public List<TheatreDocument> searchTheatres(String query) {
        if (query == null || query.trim().isEmpty()) {
            return (List<TheatreDocument>) theatreSearchRepository.findAll();
        }
        return theatreSearchRepository.findByNameContainingIgnoreCase(query);
    }
    
    public List<TheatreDocument> searchTheatresByCity(Long cityId) {
        return theatreSearchRepository.findByCityId(cityId);
    }
    
    public List<TheatreDocument> searchTheatresByCityName(String cityName) {
        return theatreSearchRepository.findByCityNameContainingIgnoreCase(cityName);
    }
    
    public List<TheatreDocument> searchTheatresByAddress(String address) {
        return theatreSearchRepository.findByAddressContainingIgnoreCase(address);
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
        if (query == null || query.trim().isEmpty()) {
            return (List<ShowDocument>) showSearchRepository.findAll();
        }
        return showSearchRepository.findByMovieTitleContainingIgnoreCase(query);
    }
    
    public List<ShowDocument> searchShowsByMovie(Long movieId) {
        return showSearchRepository.findByMovieId(movieId);
    }
    
    public List<ShowDocument> searchShowsByTheatre(Long theatreId) {
        return showSearchRepository.findByTheatreId(theatreId);
    }
    
    public List<ShowDocument> searchShowsByCity(Long cityId) {
        return showSearchRepository.findByCityId(cityId);
    }
    
    public List<ShowDocument> searchShowsByMovieAndCity(Long movieId, Long cityId) {
        return showSearchRepository.findByMovieIdAndCityId(movieId, cityId);
    }
    
    public List<ShowDocument> searchShowsByMovieAndCityAfterDateTime(Long movieId, Long cityId, LocalDateTime dateTime) {
        return showSearchRepository.findByMovieIdAndCityIdAndShowDateTimeAfter(movieId, cityId, dateTime);
    }
    
    public List<ShowDocument> searchShowsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return showSearchRepository.findByShowDateTimeBetween(startDate, endDate);
    }
    
    public List<ShowDocument> searchShowsByTheatreName(String theatreName) {
        return showSearchRepository.findByTheatreNameContainingIgnoreCase(theatreName);
    }
    
    public List<ShowDocument> searchShowsByCityName(String cityName) {
        return showSearchRepository.findByCityNameContainingIgnoreCase(cityName);
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
}