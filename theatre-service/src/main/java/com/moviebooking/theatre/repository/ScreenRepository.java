package com.moviebooking.theatre.repository;

import com.moviebooking.theatre.model.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {
    List<Screen> findByTheatreId(Long theatreId);
    
    List<Screen> findByTheatreIdAndScreenType(Long theatreId, Screen.ScreenType screenType);
}