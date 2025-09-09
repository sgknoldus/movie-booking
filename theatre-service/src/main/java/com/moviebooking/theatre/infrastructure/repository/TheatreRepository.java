package com.moviebooking.theatre.infrastructure.repository;

import com.moviebooking.theatre.domain.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TheatreRepository extends JpaRepository<Theatre, UUID> {
    
    @Query("SELECT DISTINCT t FROM Theatre t " +
           "JOIN FETCH t.screens s " +
           "JOIN FETCH s.shows sh " +
           "WHERE t.city = :city")
    List<Theatre> findByCityWithScreensAndShows(@Param("city") String city);

    List<Theatre> findByCity(String city);
}
