package com.moviebooking.movie.infrastructure.repository;

import com.moviebooking.movie.domain.search.MovieDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface MovieSearchRepository extends ElasticsearchRepository<MovieDocument, UUID> {
    List<MovieDocument> findByGenresInAndIsCurrentlyPlayingTrue(Set<String> genres);
    List<MovieDocument> findByLanguagesIn(Set<String> languages);
    List<MovieDocument> findByIsCurrentlyPlayingTrue();
}
