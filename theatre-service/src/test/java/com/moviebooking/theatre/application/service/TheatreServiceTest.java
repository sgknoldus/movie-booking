package com.moviebooking.theatre.application.service;

import com.moviebooking.theatre.domain.Screen;
import com.moviebooking.theatre.domain.Show;
import com.moviebooking.theatre.domain.Theatre;
import com.moviebooking.theatre.infrastructure.repository.ShowRepository;
import com.moviebooking.theatre.infrastructure.repository.TheatreElasticsearchRepository;
import com.moviebooking.theatre.infrastructure.repository.TheatreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TheatreServiceTest {

    @Mock
    private TheatreRepository theatreRepository;

    @Mock
    private TheatreElasticsearchRepository theatreElasticsearchRepository;

    @Mock
    private ShowRepository showRepository;

    @InjectMocks
    private TheatreService theatreService;

    private UUID movieId;
    private Theatre theatre;
    private Screen screen;
    private Show show;

    @BeforeEach
    void setUp() {
        movieId = UUID.randomUUID();
        
        theatre = Theatre.builder()
                .id(UUID.randomUUID())
                .name("Test Theatre")
                .city("Test City")
                .address("Test Address")
                .build();

        screen = Screen.builder()
                .id(UUID.randomUUID())
                .screenNumber(1)
                .totalSeats(100)
                .theatre(theatre)
                .build();

        show = Show.builder()
                .id(UUID.randomUUID())
                .movieId(movieId)
                .screen(screen)
                .showTime(LocalDateTime.now())
                .price(BigDecimal.valueOf(10.00))
                .availableSeats("[]")
                .build();

        theatre.setScreens(List.of(screen));
        screen.setShows(List.of(show));
    }

    @Test
    void searchTheatres_ShouldReturnTheatresWithShows() {
        // Given
        LocalDate searchDate = LocalDate.now();
        when(showRepository.findByMovieIdAndShowTimeBetween(any(), any(), any()))
                .thenReturn(List.of(show));

        // When
        var result = theatreService.searchTheatres(movieId, "Test City", searchDate);

        // Then
        assertThat(result).hasSize(1);
        var theatreResponse = result.get(0);
        assertThat(theatreResponse.getTheatreId()).isEqualTo(theatre.getId());
        assertThat(theatreResponse.getName()).isEqualTo(theatre.getName());
        assertThat(theatreResponse.getCity()).isEqualTo(theatre.getCity());
        assertThat(theatreResponse.getShows()).hasSize(1);
    }
}
