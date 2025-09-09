package com.moviebooking.theatre.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "theatres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Theatre {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    private String address;

    @OneToMany(mappedBy = "theatre", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Screen> screens = new HashSet<>();

    @Version
    private Long version;

    public void addScreen(Screen screen) {
        screens.add(screen);
        screen.setTheatre(this);
    }

    public void removeScreen(Screen screen) {
        screens.remove(screen);
        screen.setTheatre(null);
    }
}
