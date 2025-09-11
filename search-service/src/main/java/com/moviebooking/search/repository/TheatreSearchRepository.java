package com.moviebooking.search.repository;

import com.moviebooking.search.model.TheatreDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheatreSearchRepository extends ElasticsearchRepository<TheatreDocument, String> {
    List<TheatreDocument> findByNameContainingIgnoreCase(String name);
    List<TheatreDocument> findByCityId(Long cityId);
    List<TheatreDocument> findByCityNameContainingIgnoreCase(String cityName);
    List<TheatreDocument> findByAddressContainingIgnoreCase(String address);
    List<TheatreDocument> findByCityIdAndNameContainingIgnoreCase(Long cityId, String name);
}