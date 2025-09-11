package com.moviebooking.search.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.moviebooking.search.model.CityDocument;
import com.moviebooking.search.model.ShowDocument;
import com.moviebooking.search.model.TheatreDocument;
import com.moviebooking.search.repository.CitySearchRepository;
import com.moviebooking.search.repository.ShowSearchRepository;
import com.moviebooking.search.repository.TheatreSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchIndexService {
    private final CitySearchRepository citySearchRepository;
    private final TheatreSearchRepository theatreSearchRepository;
    private final ShowSearchRepository showSearchRepository;
    
    public void indexCity(JsonNode cityData) {
        try {
            CityDocument cityDoc = new CityDocument();
            cityDoc.setId(cityData.get("id").asText());
            cityDoc.setName(cityData.get("name").asText());
            cityDoc.setState(cityData.get("state").asText());
            cityDoc.setCountry(cityData.get("country").asText());
            
            if (cityData.has("zipCode") && !cityData.get("zipCode").isNull()) {
                cityDoc.setZipCode(cityData.get("zipCode").asText());
            }
            
            cityDoc.setCreatedAt(parseDateTime(cityData.get("createdAt").asText()));
            cityDoc.setUpdatedAt(parseDateTime(cityData.get("updatedAt").asText()));
            
            citySearchRepository.save(cityDoc);
            log.info("Indexed city: {} with ID: {}", cityDoc.getName(), cityDoc.getId());
            
        } catch (Exception e) {
            log.error("Failed to index city: {}", e.getMessage(), e);
        }
    }
    
    public void indexTheatre(JsonNode theatreData) {
        try {
            TheatreDocument theatreDoc = new TheatreDocument();
            theatreDoc.setId(theatreData.get("id").asText());
            theatreDoc.setName(theatreData.get("name").asText());
            theatreDoc.setAddress(theatreData.get("address").asText());
            
            if (theatreData.has("phoneNumber") && !theatreData.get("phoneNumber").isNull()) {
                theatreDoc.setPhoneNumber(theatreData.get("phoneNumber").asText());
            }
            
            if (theatreData.has("email") && !theatreData.get("email").isNull()) {
                theatreDoc.setEmail(theatreData.get("email").asText());
            }
            
            if (theatreData.has("latitude") && !theatreData.get("latitude").isNull() &&
                theatreData.has("longitude") && !theatreData.get("longitude").isNull()) {
                double lat = theatreData.get("latitude").asDouble();
                double lon = theatreData.get("longitude").asDouble();
                theatreDoc.setLocation(new GeoPoint(lat, lon));
            }
            
            theatreDoc.setCityId(theatreData.get("cityId").asLong());
            theatreDoc.setCityName(theatreData.get("cityName").asText());
            
            theatreDoc.setCreatedAt(parseDateTime(theatreData.get("createdAt").asText()));
            theatreDoc.setUpdatedAt(parseDateTime(theatreData.get("updatedAt").asText()));
            
            theatreSearchRepository.save(theatreDoc);
            log.info("Indexed theatre: {} with ID: {}", theatreDoc.getName(), theatreDoc.getId());
            
        } catch (Exception e) {
            log.error("Failed to index theatre: {}", e.getMessage(), e);
        }
    }
    
    public void indexShow(JsonNode showData) {
        try {
            ShowDocument showDoc = new ShowDocument();
            showDoc.setId(showData.get("id").asText());
            showDoc.setMovieId(showData.get("movieId").asLong());
            showDoc.setMovieTitle(showData.get("movieTitle").asText());
            showDoc.setShowDateTime(parseDateTime(showData.get("showDateTime").asText()));
            showDoc.setEndDateTime(parseDateTime(showData.get("endDateTime").asText()));
            showDoc.setPrice(new BigDecimal(showData.get("price").asText()));
            showDoc.setAvailableSeats(showData.get("availableSeats").asInt());
            showDoc.setStatus(showData.get("status").asText());
            
            showDoc.setScreenId(showData.get("screenId").asLong());
            showDoc.setScreenName(showData.get("screenName").asText());
            
            showDoc.setTheatreId(showData.get("theatreId").asLong());
            showDoc.setTheatreName(showData.get("theatreName").asText());
            
            showDoc.setCityId(showData.get("cityId").asLong());
            showDoc.setCityName(showData.get("cityName").asText());
            
            showDoc.setCreatedAt(parseDateTime(showData.get("createdAt").asText()));
            showDoc.setUpdatedAt(parseDateTime(showData.get("updatedAt").asText()));
            
            showSearchRepository.save(showDoc);
            log.info("Indexed show: {} with ID: {}", showDoc.getMovieTitle(), showDoc.getId());
            
        } catch (Exception e) {
            log.error("Failed to index show: {}", e.getMessage(), e);
        }
    }
    
    public void deleteCity(String cityId) {
        try {
            citySearchRepository.deleteById(cityId);
            log.info("Deleted city with ID: {}", cityId);
        } catch (Exception e) {
            log.error("Failed to delete city: {}", e.getMessage(), e);
        }
    }
    
    public void deleteTheatre(String theatreId) {
        try {
            theatreSearchRepository.deleteById(theatreId);
            log.info("Deleted theatre with ID: {}", theatreId);
        } catch (Exception e) {
            log.error("Failed to delete theatre: {}", e.getMessage(), e);
        }
    }
    
    public void deleteShow(String showId) {
        try {
            showSearchRepository.deleteById(showId);
            log.info("Deleted show with ID: {}", showId);
        } catch (Exception e) {
            log.error("Failed to delete show: {}", e.getMessage(), e);
        }
    }
    
    public void updateShowAvailableSeats(Long showId) {
        // This would typically query the theatre service or calculate from seat events
        // For now, we'll just log it as this requires additional integration
        log.info("Request to update available seats for show ID: {}", showId);
    }
    
    public void updateShowSeatAvailability(Long showId, List<String> seatNumbers, boolean available) {
        try {
            showSearchRepository.findById(showId.toString()).ifPresent(showDoc -> {
                if (!available) {
                    // Seats are being booked, decrease available seats
                    int currentAvailable = showDoc.getAvailableSeats();
                    int newAvailable = Math.max(0, currentAvailable - seatNumbers.size());
                    showDoc.setAvailableSeats(newAvailable);
                } else {
                    // Seats are being released, increase available seats
                    int currentAvailable = showDoc.getAvailableSeats();
                    int newAvailable = currentAvailable + seatNumbers.size();
                    showDoc.setAvailableSeats(newAvailable);
                }
                
                showDoc.setUpdatedAt(LocalDateTime.now());
                showSearchRepository.save(showDoc);
                
                log.info("Updated show {} seat availability. Seats {} set to available={}", 
                        showId, seatNumbers, available);
            });
        } catch (Exception e) {
            log.error("Failed to update seat availability for show: {}", showId, e);
        }
    }
    
    public void reindexAllData() {
        log.info("Starting full reindex of all data...");
        // This would typically call the Theatre Service APIs to get all data and reindex
        // Implementation would depend on how you want to handle full reindexing
    }
    
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            log.warn("Null or empty datetime string, using current time");
            return LocalDateTime.now();
        }
        
        try {
            // Try parsing as full ISO datetime first (e.g., "2025-09-11T14:30:00")
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e1) {
            try {
                // Try parsing as date only and add midnight time (e.g., "2025-09-11")
                return LocalDateTime.parse(dateTimeStr + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e2) {
                try {
                    // Try parsing with different datetime patterns
                    return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (Exception e3) {
                    log.warn("Failed to parse datetime '{}' with all attempted formats, using current time. Errors: ISO_LOCAL_DATE_TIME={}, ISO_DATE_with_midnight={}, yyyy-MM-dd_HH:mm:ss={}", 
                        dateTimeStr, e1.getMessage(), e2.getMessage(), e3.getMessage());
                    return LocalDateTime.now();
                }
            }
        }
    }
}