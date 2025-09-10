package com.moviebooking.theatre.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebooking.theatre.application.dto.TheatreSearchResponse;
import com.moviebooking.theatre.application.dto.TheatreRequest;
import com.moviebooking.theatre.application.dto.TheatreResponse;
import com.moviebooking.theatre.application.dto.ShowRequest;
import com.moviebooking.theatre.domain.Show;
import com.moviebooking.theatre.domain.Theatre;
import com.moviebooking.theatre.domain.search.TheatreDocument;
import com.moviebooking.theatre.infrastructure.repository.ShowRepository;
import com.moviebooking.theatre.infrastructure.repository.TheatreElasticsearchRepository;
import com.moviebooking.theatre.infrastructure.repository.TheatreRepository;
import com.moviebooking.theatre.infrastructure.repository.ScreenRepository;
import com.moviebooking.theatre.domain.Screen;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TheatreService {

    private final TheatreElasticsearchRepository theatreElasticsearchRepository;
    private final ShowRepository showRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;

    @Transactional(readOnly = true)
    public List<TheatreSearchResponse> searchTheatres(UUID movieId, String city, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Show> shows = showRepository.findByMovieIdAndShowTimeBetween(movieId, startOfDay, endOfDay);
        
        return shows.stream()
                .map(show -> {
                    Theatre theatre = show.getScreen().getTheatre();
                    return TheatreSearchResponse.builder()
                            .theatreId(theatre.getId())
                            .name(theatre.getName())
                            .city(theatre.getCity())
                            .address(theatre.getAddress())
                            .shows(List.of(TheatreSearchResponse.ShowInfo.builder()
                                    .showId(show.getId())
                                    .showTime(show.getShowTime().toString())
                                    .screenNumber(show.getScreen().getScreenNumber().toString())
                                    .availableSeats(parseAvailableSeats(show.getAvailableSeats()))
                                    .price(show.getPrice().doubleValue())
                                    .build()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void indexTheatre(Theatre theatre, GeoPoint location) {
        TheatreDocument document = TheatreDocument.builder()
                .id(theatre.getId())
                .name(theatre.getName())
                .city(theatre.getCity())
                .address(theatre.getAddress())
                .location(location)
                .build();
        
        theatreElasticsearchRepository.save(document);
    }

    @Transactional(readOnly = true)
    public ShowResponse getShow(UUID showId) {
        Show show = showRepository.findByIdWithTheatre(showId)
                .orElseThrow(() -> new RuntimeException("Show not found with id: " + showId));
        
        return new ShowResponse(
                show.getId(),
                show.getScreen().getTheatre().getId(),
                show.getMovieId(),
                show.getShowTime().toString(),
                show.getPrice().doubleValue(),
                show.getAvailableSeats()
        );
    }

    private List<String> parseAvailableSeats(String availableSeatsJson) {
        if (availableSeatsJson == null || availableSeatsJson.isBlank()) {
            return List.of();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(availableSeatsJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parsing available seats JSON: {}", e.getMessage());
            return List.of();
        }
    }

    // Theatre Management Methods
    @Transactional
    public TheatreResponse createTheatre(TheatreRequest request) {
        Theatre theatre = Theatre.builder()
                .name(request.getName())
                .city(request.getCity())
                .address(request.getAddress())
                .build();
        
        theatre = theatreRepository.save(theatre);
        
        return TheatreResponse.builder()
                .id(theatre.getId())
                .name(theatre.getName())
                .city(theatre.getCity())
                .address(theatre.getAddress())
                .totalScreens(theatre.getScreens().size())
                .build();
    }

    @Transactional(readOnly = true)
    public TheatreResponse getTheatre(UUID theatreId) {
        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new RuntimeException("Theatre not found with id: " + theatreId));
        
        return TheatreResponse.builder()
                .id(theatre.getId())
                .name(theatre.getName())
                .city(theatre.getCity())
                .address(theatre.getAddress())
                .totalScreens(theatre.getScreens().size())
                .build();
    }

    @Transactional(readOnly = true)
    public List<TheatreResponse> getAllTheatres() {
        return theatreRepository.findAll().stream()
                .map(theatre -> TheatreResponse.builder()
                        .id(theatre.getId())
                        .name(theatre.getName())
                        .city(theatre.getCity())
                        .address(theatre.getAddress())
                        .totalScreens(theatre.getScreens().size())
                        .build())
                .collect(Collectors.toList());
    }

    // Show Management Methods  
    @Transactional
    public com.moviebooking.theatre.application.dto.ShowResponse createShow(ShowRequest request) {
        Show show = Show.builder()
                .movieId(request.getMovieId())
                .showTime(request.getShowTime())
                .price(request.getPrice())
                .availableSeats(convertSeatsToJson(request.getAvailableSeats()))
                .build();
        
        // Find screen and associate
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new RuntimeException("Screen not found with id: " + request.getScreenId()));
        
        show.setScreen(screen);
        show = showRepository.save(show);
        
        return com.moviebooking.theatre.application.dto.ShowResponse.builder()
                .id(show.getId())
                .movieId(show.getMovieId())
                .screenId(screen.getId())
                .theatreId(screen.getTheatre().getId())
                .theatreName(screen.getTheatre().getName())
                .screenNumber(screen.getScreenNumber())
                .showTime(show.getShowTime())
                .price(show.getPrice())
                .availableSeats(parseAvailableSeats(show.getAvailableSeats()))
                .build();
    }

    private String convertSeatsToJson(List<String> seats) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(seats);
        } catch (JsonProcessingException e) {
            log.error("Error converting seats to JSON: {}", e.getMessage());
            return "[]";
        }
    }

    public record ShowResponse(
            UUID showId,
            UUID theatreId,
            UUID movieId,
            String showTime,
            Double price,
            String availableSeats
    ) {}
}
