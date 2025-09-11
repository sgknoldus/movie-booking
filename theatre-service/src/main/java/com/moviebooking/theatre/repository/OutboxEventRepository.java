package com.moviebooking.theatre.repository;

import com.moviebooking.theatre.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxEvent.EventStatus status);
    
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'FAILED' AND e.retryCount < :maxRetries AND e.createdAt > :cutoffTime ORDER BY e.createdAt ASC")
    List<OutboxEvent> findFailedEventsForRetry(@Param("maxRetries") Integer maxRetries, 
                                              @Param("cutoffTime") LocalDateTime cutoffTime);
    
    void deleteByStatusAndProcessedAtBefore(OutboxEvent.EventStatus status, LocalDateTime cutoffTime);
}