package com.moviebooking.theatre.service;

import com.moviebooking.theatre.exception.ResourceNotFoundException;
import com.moviebooking.theatre.exception.DuplicateResourceException;
import com.moviebooking.theatre.model.*;
import com.moviebooking.theatre.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CityService {
    private final CityRepository cityRepository;
    private final OutboxEventService outboxEventService;
    
    @Transactional
    public CityResponse createCity(CityRequest request) {
        log.info("Creating new city: {}", request.getName());
        
        if (cityRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("City with name '" + request.getName() + "' already exists");
        }
        
        City city = new City();
        city.setName(request.getName());
        city.setState(request.getState());
        city.setCountry(request.getCountry());
        city.setZipCode(request.getZipCode());
        
        City savedCity = cityRepository.save(city);
        
        outboxEventService.publishCityEvent("CITY_CREATED", savedCity);
        
        return mapToResponse(savedCity);
    }
    
    @Transactional
    public CityResponse updateCity(Long id, CityRequest request) {
        log.info("Updating city with ID: {}", id);
        
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with ID: " + id));
        
        if (!city.getName().equalsIgnoreCase(request.getName()) && 
            cityRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("City with name '" + request.getName() + "' already exists");
        }
        
        city.setName(request.getName());
        city.setState(request.getState());
        city.setCountry(request.getCountry());
        city.setZipCode(request.getZipCode());
        
        City savedCity = cityRepository.save(city);
        
        outboxEventService.publishCityEvent("CITY_UPDATED", savedCity);
        
        return mapToResponse(savedCity);
    }
    
    public CityResponse getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with ID: " + id));
        return mapToResponse(city);
    }
    
    public List<CityResponse> getAllCities() {
        return cityRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<CityResponse> searchCitiesByName(String name) {
        return cityRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteCity(Long id) {
        log.info("Deleting city with ID: {}", id);
        
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with ID: " + id));
        
        cityRepository.delete(city);
        
        outboxEventService.publishCityEvent("CITY_DELETED", city);
    }
    
    private CityResponse mapToResponse(City city) {
        return new CityResponse(
                city.getId(),
                city.getName(),
                city.getState(),
                city.getCountry(),
                city.getZipCode(),
                city.getCreatedAt(),
                city.getUpdatedAt()
        );
    }
}