package com.moviebooking.theatre.service;

import com.moviebooking.theatre.exception.ResourceNotFoundException;
import com.moviebooking.theatre.model.*;
import com.moviebooking.theatre.repository.CityRepository;
import com.moviebooking.theatre.repository.TheatreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TheatreService {
    private final TheatreRepository theatreRepository;
    private final CityRepository cityRepository;
    private final OutboxEventService outboxEventService;
    private final CityService cityService;
    
    @Transactional("transactionManager")
    public TheatreResponse createTheatre(TheatreRequest request) {
        log.info("Creating new theatre: {}", request.getName());
        
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found with ID: " + request.getCityId()));
        
        Theatre theatre = new Theatre();
        theatre.setName(request.getName());
        theatre.setAddress(request.getAddress());
        theatre.setPhoneNumber(request.getPhoneNumber());
        theatre.setEmail(request.getEmail());
        theatre.setLatitude(convertToBigDecimal(request.getLatitude()));
        theatre.setLongitude(convertToBigDecimal(request.getLongitude()));
        theatre.setCity(city);
        
        Theatre savedTheatre = theatreRepository.save(theatre);
        
        outboxEventService.publishTheatreEvent("THEATRE_CREATED", savedTheatre);
        
        return mapToResponse(savedTheatre);
    }
    
    @Transactional("transactionManager")
    public TheatreResponse updateTheatre(Long id, TheatreRequest request) {
        log.info("Updating theatre with ID: {}", id);
        
        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with ID: " + id));
        
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found with ID: " + request.getCityId()));
        
        theatre.setName(request.getName());
        theatre.setAddress(request.getAddress());
        theatre.setPhoneNumber(request.getPhoneNumber());
        theatre.setEmail(request.getEmail());
        theatre.setLatitude(convertToBigDecimal(request.getLatitude()));
        theatre.setLongitude(convertToBigDecimal(request.getLongitude()));
        theatre.setCity(city);
        
        Theatre savedTheatre = theatreRepository.save(theatre);
        
        outboxEventService.publishTheatreEvent("THEATRE_UPDATED", savedTheatre);
        
        return mapToResponse(savedTheatre);
    }
    
    public TheatreResponse getTheatreById(Long id) {
        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with ID: " + id));
        return mapToResponse(theatre);
    }
    
    public List<TheatreResponse> getAllTheatres() {
        return theatreRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<TheatreResponse> getTheatresByCity(Long cityId) {
        return theatreRepository.findByCityId(cityId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<TheatreResponse> searchTheatresByName(String name) {
        return theatreRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<TheatreResponse> searchTheatresByCityAndName(Long cityId, String name) {
        return theatreRepository.findByCityIdAndNameContainingIgnoreCase(cityId, name).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<TheatreResponse> getTheatresNearLocation(Double latitude, Double longitude, Double radiusKm) {
        Double radiusSquared = (radiusKm / 111.0) * (radiusKm / 111.0);
        return theatreRepository.findTheatresWithinRadius(latitude, longitude, radiusSquared).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional("transactionManager")
    public void deleteTheatre(Long id) {
        log.info("Deleting theatre with ID: {}", id);
        
        Theatre theatre = theatreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with ID: " + id));
        
        theatreRepository.delete(theatre);
        
        outboxEventService.publishTheatreEvent("THEATRE_DELETED", theatre);
    }
    
    private TheatreResponse mapToResponse(Theatre theatre) {
        return new TheatreResponse(
                theatre.getId(),
                theatre.getName(),
                theatre.getAddress(),
                theatre.getPhoneNumber(),
                theatre.getEmail(),
                convertToDouble(theatre.getLatitude()),
                convertToDouble(theatre.getLongitude()),
                cityService.getCityById(theatre.getCity().getId()),
                theatre.getCreatedAt(),
                theatre.getUpdatedAt()
        );
    }
    
    private BigDecimal convertToBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
    
    private Double convertToDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }
}