package com.moviebooking.theatre.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "screens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screen {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theatre theatre;

    @Column(name = "screen_number")
    private Integer screenNumber;

    @Column(name = "total_seats")
    private Integer totalSeats;

    @Column(name = "seat_layout", columnDefinition = "jsonb")
    private String seatLayout;

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Show> shows = new HashSet<>();

    @Version
    private Long version;

    public void addShow(Show show) {
        shows.add(show);
        if (show.getScreen() != this) {
            show.setScreen(this);
        }
    }

    public void removeShow(Show show) {
        shows.remove(show);
        if (show.getScreen() == this) {
            show.setScreen(null);
        }
    }
}
