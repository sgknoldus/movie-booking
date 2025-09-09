package com.moviebooking.theatre.infrastructure.repository;

import com.moviebooking.theatre.domain.search.TheatreDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.UUID;

public interface TheatreElasticsearchRepository extends ElasticsearchRepository<TheatreDocument, UUID> {
    List<TheatreDocument> findByCity(String city);
}
