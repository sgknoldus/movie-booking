package com.moviebooking.theatre.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "movie_id", nullable = false)
    private UUID movieId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Screen screen;

    @Column(name = "show_time", nullable = false)
    private LocalDateTime showTime;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "available_seats", columnDefinition = "jsonb")
    private String availableSeats;

    @Version
    private Long version;
}
