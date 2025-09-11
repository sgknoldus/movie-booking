package com.moviebooking.search.repository;

import com.moviebooking.search.model.ShowDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowSearchRepository extends ElasticsearchRepository<ShowDocument, String> {
    List<ShowDocument> findByMovieId(Long movieId);
    List<ShowDocument> findByTheatreId(Long theatreId);
    List<ShowDocument> findByCityId(Long cityId);
    List<ShowDocument> findByMovieIdAndCityId(Long movieId, Long cityId);
    List<ShowDocument> findByMovieIdAndCityIdAndShowDateTimeAfter(Long movieId, Long cityId, LocalDateTime dateTime);
    List<ShowDocument> findByMovieTitleContainingIgnoreCase(String movieTitle);
    List<ShowDocument> findByTheatreNameContainingIgnoreCase(String theatreName);
    List<ShowDocument> findByCityNameContainingIgnoreCase(String cityName);
    List<ShowDocument> findByShowDateTimeBetween(LocalDateTime start, LocalDateTime end);
}