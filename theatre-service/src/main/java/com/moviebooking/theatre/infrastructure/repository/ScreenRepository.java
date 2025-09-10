package com.moviebooking.theatre.infrastructure.repository;

import com.moviebooking.theatre.domain.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScreenRepository extends JpaRepository<Screen, UUID> {
}