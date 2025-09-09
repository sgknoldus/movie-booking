package com.moviebooking.theatre.application.service;

import com.moviebooking.theatre.application.dto.TheatreSearchResponse;
import com.moviebooking.theatre.domain.Show;
import com.moviebooking.theatre.domain.Theatre;
import com.moviebooking.theatre.domain.search.TheatreDocument;
import com.moviebooking.theatre.infrastructure.repository.ShowRepository;
import com.moviebooking.theatre.infrastructure.repository.TheatreElasticsearchRepository;
import com.moviebooking.theatre.infrastructure.repository.TheatreRepository;
import lombok.RequiredArgsConstructor;
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
public class TheatreService {

    private final TheatreRepository theatreRepository;
    private final TheatreElasticsearchRepository theatreElasticsearchRepository;
    private final ShowRepository showRepository;

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

    private List<String> parseAvailableSeats(String availableSeatsJson) {
        // TODO: Implement JSON parsing of available seats
        return List.of();
    }
}
