package com.moviebooking.theatre.service;

import com.moviebooking.theatre.exception.BusinessLogicException;
import com.moviebooking.theatre.exception.ResourceNotFoundException;
import com.moviebooking.theatre.model.*;
import com.moviebooking.theatre.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowService {
    private final ShowRepository showRepository;
    private final ScreenRepository screenRepository;
    private final TheatreRepository theatreRepository;
    private final SeatAvailabilityRepository seatAvailabilityRepository;
    private final OutboxEventService outboxEventService;
    private final ScreenService screenService;
    private final TheatreService theatreService;
    
    @Transactional
    public ShowResponse createShow(ShowRequest request) {
        log.info("Creating new show for movie: {}", request.getMovieTitle());
        
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with ID: " + request.getScreenId()));
        
        Theatre theatre = theatreRepository.findById(request.getTheatreId())
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with ID: " + request.getTheatreId()));
        
        validateShowTiming(request.getScreenId(), request.getShowDateTime(), request.getEndDateTime());
        
        Show show = new Show();
        show.setMovieId(request.getMovieId());
        show.setMovieTitle(request.getMovieTitle());
        show.setShowDateTime(request.getShowDateTime());
        show.setEndDateTime(request.getEndDateTime());
        show.setPrice(request.getPrice());
        show.setAvailableSeats(screen.getTotalSeats());
        show.setStatus(Show.ShowStatus.SCHEDULED);
        show.setScreen(screen);
        show.setTheatre(theatre);
        
        Show savedShow = showRepository.save(show);
        
        generateSeatAvailability(savedShow, screen);
        
        outboxEventService.publishShowEvent("SHOW_CREATED", savedShow);
        
        return mapToResponse(savedShow);
    }
    
    @Transactional
    public ShowResponse updateShow(Long id, ShowRequest request) {
        log.info("Updating show with ID: {}", id);
        
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with ID: " + id));
        
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with ID: " + request.getScreenId()));
        
        Theatre theatre = theatreRepository.findById(request.getTheatreId())
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with ID: " + request.getTheatreId()));
        
        if (!show.getShowDateTime().equals(request.getShowDateTime()) || 
            !show.getEndDateTime().equals(request.getEndDateTime()) ||
            !show.getScreen().getId().equals(request.getScreenId())) {
            validateShowTiming(request.getScreenId(), request.getShowDateTime(), request.getEndDateTime());
        }
        
        show.setMovieId(request.getMovieId());
        show.setMovieTitle(request.getMovieTitle());
        show.setShowDateTime(request.getShowDateTime());
        show.setEndDateTime(request.getEndDateTime());
        show.setPrice(request.getPrice());
        show.setScreen(screen);
        show.setTheatre(theatre);
        
        Show savedShow = showRepository.save(show);
        
        outboxEventService.publishShowEvent("SHOW_UPDATED", savedShow);
        
        return mapToResponse(savedShow);
    }
    
    public ShowResponse getShowById(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with ID: " + id));
        return mapToResponse(show);
    }
    
    public List<ShowResponse> getAllShows() {
        return showRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<ShowResponse> getShowsByTheatre(Long theatreId) {
        return showRepository.findByTheatreId(theatreId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<ShowResponse> getShowsByMovie(Long movieId) {
        return showRepository.findByMovieId(movieId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<ShowResponse> getShowsByMovieAndCity(Long movieId, Long cityId, LocalDateTime fromDateTime) {
        return showRepository.findByMovieIdAndCityIdAndShowDateTimeAfter(movieId, cityId, fromDateTime).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<ShowResponse> getShowsByTheatreAndDateRange(Long theatreId, LocalDateTime startTime, LocalDateTime endTime) {
        return showRepository.findByTheatreIdAndShowDateTimeBetween(theatreId, startTime, endTime).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteShow(Long id) {
        log.info("Deleting show with ID: {}", id);
        
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with ID: " + id));
        
        showRepository.delete(show);
        
        outboxEventService.publishShowEvent("SHOW_DELETED", show);
    }
    
    private void validateShowTiming(Long screenId, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new BusinessLogicException("Show start time must be before end time");
        }
        
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new BusinessLogicException("Show start time must be in the future");
        }
        
        List<Show> conflictingShows = showRepository.findConflictingShows(screenId, startTime, endTime);
        if (!conflictingShows.isEmpty()) {
            throw new BusinessLogicException("Screen is already booked during this time slot");
        }
    }
    
    private void generateSeatAvailability(Show show, Screen screen) {
        for (int row = 1; row <= 10; row++) {
            char rowLetter = (char) ('A' + row - 1);
            int seatsPerRow = screen.getTotalSeats() / 10;
            
            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                SeatAvailability seat = new SeatAvailability();
                seat.setSeatNumber(rowLetter + String.valueOf(seatNum));
                seat.setRowNumber(String.valueOf(rowLetter));
                seat.setSeatType(determineSeatType(row, seatNum, seatsPerRow));
                seat.setStatus(SeatAvailability.SeatStatus.AVAILABLE);
                seat.setShow(show);
                
                seatAvailabilityRepository.save(seat);
            }
        }
    }
    
    private SeatAvailability.SeatType determineSeatType(int row, int seatNum, int seatsPerRow) {
        if (row <= 2) {
            return SeatAvailability.SeatType.PREMIUM;
        } else if (row >= 8 && (seatNum == 1 || seatNum == seatsPerRow)) {
            return SeatAvailability.SeatType.WHEELCHAIR_ACCESSIBLE;
        } else if (row >= 7) {
            return SeatAvailability.SeatType.VIP;
        }
        return SeatAvailability.SeatType.REGULAR;
    }
    
    private ShowResponse mapToResponse(Show show) {
        return new ShowResponse(
                show.getId(),
                show.getMovieId(),
                show.getMovieTitle(),
                show.getShowDateTime(),
                show.getEndDateTime(),
                show.getPrice(),
                show.getAvailableSeats(),
                show.getStatus(),
                screenService.getScreenById(show.getScreen().getId()),
                theatreService.getTheatreById(show.getTheatre().getId()),
                show.getCreatedAt(),
                show.getUpdatedAt()
        );
    }
}