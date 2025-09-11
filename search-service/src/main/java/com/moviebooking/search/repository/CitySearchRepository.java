package com.moviebooking.search.repository;

import com.moviebooking.search.model.CityDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitySearchRepository extends ElasticsearchRepository<CityDocument, String> {
    List<CityDocument> findByNameContainingIgnoreCase(String name);
    List<CityDocument> findByStateIgnoreCase(String state);
    List<CityDocument> findByCountryIgnoreCase(String country);
}