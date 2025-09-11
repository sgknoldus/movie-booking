package com.moviebooking.theatre.service;

import com.moviebooking.theatre.exception.ResourceNotFoundException;
import com.moviebooking.theatre.model.*;
import com.moviebooking.theatre.repository.ScreenRepository;
import com.moviebooking.theatre.repository.TheatreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScreenService {
    private final ScreenRepository screenRepository;
    private final TheatreRepository theatreRepository;
    private final OutboxEventService outboxEventService;
    private final TheatreService theatreService;
    
    @Transactional
    public ScreenResponse createScreen(ScreenRequest request) {
        log.info("Creating new screen: {}", request.getName());
        
        Theatre theatre = theatreRepository.findById(request.getTheatreId())
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with ID: " + request.getTheatreId()));
        
        Screen screen = new Screen();
        screen.setName(request.getName());
        screen.setTotalSeats(request.getTotalSeats());
        screen.setScreenType(request.getScreenType());
        screen.setTheatre(theatre);
        
        Screen savedScreen = screenRepository.save(screen);
        
        outboxEventService.publishScreenEvent("SCREEN_CREATED", savedScreen);
        
        return mapToResponse(savedScreen);
    }
    
    @Transactional
    public ScreenResponse updateScreen(Long id, ScreenRequest request) {
        log.info("Updating screen with ID: {}", id);
        
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with ID: " + id));
        
        Theatre theatre = theatreRepository.findById(request.getTheatreId())
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with ID: " + request.getTheatreId()));
        
        screen.setName(request.getName());
        screen.setTotalSeats(request.getTotalSeats());
        screen.setScreenType(request.getScreenType());
        screen.setTheatre(theatre);
        
        Screen savedScreen = screenRepository.save(screen);
        
        outboxEventService.publishScreenEvent("SCREEN_UPDATED", savedScreen);
        
        return mapToResponse(savedScreen);
    }
    
    public ScreenResponse getScreenById(Long id) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with ID: " + id));
        return mapToResponse(screen);
    }
    
    public List<ScreenResponse> getAllScreens() {
        return screenRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<ScreenResponse> getScreensByTheatre(Long theatreId) {
        return screenRepository.findByTheatreId(theatreId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<ScreenResponse> getScreensByTheatreAndType(Long theatreId, Screen.ScreenType screenType) {
        return screenRepository.findByTheatreIdAndScreenType(theatreId, screenType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteScreen(Long id) {
        log.info("Deleting screen with ID: {}", id);
        
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with ID: " + id));
        
        screenRepository.delete(screen);
        
        outboxEventService.publishScreenEvent("SCREEN_DELETED", screen);
    }
    
    private ScreenResponse mapToResponse(Screen screen) {
        return new ScreenResponse(
                screen.getId(),
                screen.getName(),
                screen.getTotalSeats(),
                screen.getScreenType(),
                theatreService.getTheatreById(screen.getTheatre().getId()),
                screen.getCreatedAt(),
                screen.getUpdatedAt()
        );
    }
}