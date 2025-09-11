package com.moviebooking.theatre.repository;

import com.moviebooking.theatre.model.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre, Long> {
    List<Theatre> findByCityId(Long cityId);
    
    @Query("SELECT t FROM Theatre t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Theatre> findByNameContainingIgnoreCase(@Param("name") String name);
    
    @Query("SELECT t FROM Theatre t WHERE t.city.id = :cityId AND LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Theatre> findByCityIdAndNameContainingIgnoreCase(@Param("cityId") Long cityId, @Param("name") String name);
    
    @Query("SELECT t FROM Theatre t WHERE " +
           "(:latitude - t.latitude) * (:latitude - t.latitude) + " +
           "(:longitude - t.longitude) * (:longitude - t.longitude) <= :radiusSquared")
    List<Theatre> findTheatresWithinRadius(@Param("latitude") Double latitude, 
                                         @Param("longitude") Double longitude, 
                                         @Param("radiusSquared") Double radiusSquared);
}