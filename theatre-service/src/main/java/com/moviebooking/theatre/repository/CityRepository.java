package com.moviebooking.theatre.repository;

import com.moviebooking.theatre.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByNameIgnoreCase(String name);
    
    List<City> findByStateIgnoreCase(String state);
    
    List<City> findByCountryIgnoreCase(String country);
    
    @Query("SELECT c FROM City c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<City> findByNameContainingIgnoreCase(@Param("name") String name);
    
    boolean existsByNameIgnoreCase(String name);
}