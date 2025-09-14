package com.moviebooking.search.config;

import com.moviebooking.search.model.CityDocument;
import com.moviebooking.search.model.ShowDocument;
import com.moviebooking.search.model.TheatreDocument;
import com.moviebooking.search.repository.CitySearchRepository;
import com.moviebooking.search.repository.ShowSearchRepository;
import com.moviebooking.search.repository.TheatreSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchDataInitializer implements CommandLineRunner {

    private final CitySearchRepository citySearchRepository;
    private final TheatreSearchRepository theatreSearchRepository;
    private final ShowSearchRepository showSearchRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting search data initialization...");

        // Check if data already exists to avoid duplicates
        if (citySearchRepository.count() > 0) {
            log.info("Search data already exists, skipping initialization");
            return;
        }

        try {
            initializeCities();
            initializeTheatres();
            initializeShows();
            log.info("Search data initialization completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize search data", e);
        }
    }

    private void initializeCities() {
        log.info("Initializing cities data...");
        LocalDateTime now = LocalDateTime.now();

        List<CityDocument> cities = Arrays.asList(
            createCity("1", "Mumbai", "Maharashtra", "India", "400001", now),
            createCity("2", "Delhi", "Delhi", "India", "110001", now),
            createCity("3", "Bangalore", "Karnataka", "India", "560001", now),
            createCity("4", "Chennai", "Tamil Nadu", "India", "600001", now),
            createCity("5", "Kolkata", "West Bengal", "India", "700001", now),
            createCity("6", "Hyderabad", "Telangana", "India", "500001", now),
            createCity("7", "Pune", "Maharashtra", "India", "411001", now),
            createCity("8", "Ahmedabad", "Gujarat", "India", "380001", now),
            createCity("9", "Surat", "Gujarat", "India", "395001", now),
            createCity("10", "Jaipur", "Rajasthan", "India", "302001", now)
        );

        citySearchRepository.saveAll(cities);
        log.info("Initialized {} cities", cities.size());
    }

    private void initializeTheatres() {
        log.info("Initializing theatres data...");
        LocalDateTime now = LocalDateTime.now();

        List<TheatreDocument> theatres = Arrays.asList(
            // Mumbai theatres
            createTheatre("1", "INOX R-City Mall", "R-City Mall, Ghatkopar West, Mumbai", "+91-22-67890123",
                "rcity@inoxmovies.com", 19.0862, 72.9093, 1L, "Mumbai", now),
            createTheatre("2", "PVR Phoenix Mills", "Phoenix Mills, Lower Parel, Mumbai", "+91-22-24305667",
                "phoenix@pvr.co.in", 19.0135, 72.8298, 1L, "Mumbai", now),
            createTheatre("3", "Cinepolis Fun Republic", "Fun Republic Mall, Andheri West, Mumbai", "+91-22-40001234",
                "andheri@cinepolis.co.in", 19.1368, 72.8269, 1L, "Mumbai", now),

            // Delhi theatres
            createTheatre("4", "PVR Select City Walk", "Select City Walk, Saket, New Delhi", "+91-11-29565000",
                "saket@pvr.co.in", 28.5245, 77.2066, 2L, "Delhi", now),
            createTheatre("5", "INOX Nehru Place", "Nehru Place, New Delhi", "+91-11-26228800",
                "nehruplace@inoxmovies.com", 28.5506, 77.2506, 2L, "Delhi", now),
            createTheatre("6", "DT Cinemas DLF Mall", "DLF Mall of India, Noida", "+91-120-4567890",
                "dlf@dtcinemas.com", 28.5677, 77.3250, 2L, "Delhi", now),

            // Bangalore theatres
            createTheatre("7", "INOX Garuda Mall", "Garuda Mall, Magrath Road, Bangalore", "+91-80-25599000",
                "garuda@inoxmovies.com", 12.9716, 77.5946, 3L, "Bangalore", now),
            createTheatre("8", "PVR Forum Mall", "Forum Mall, Koramangala, Bangalore", "+91-80-41127300",
                "forum@pvr.co.in", 12.9279, 77.6271, 3L, "Bangalore", now),
            createTheatre("9", "Cinepolis Royal Meenakshi", "Royal Meenakshi Mall, Bannerghatta Road, Bangalore", "+91-80-67890123",
                "royal@cinepolis.co.in", 12.8955, 77.5937, 3L, "Bangalore", now),

            // Chennai theatres
            createTheatre("10", "INOX Express Avenue", "Express Avenue Mall, Royapettah, Chennai", "+91-44-28578800",
                "express@inoxmovies.com", 13.0569, 80.2570, 4L, "Chennai", now),
            createTheatre("11", "PVR Ampa Skywalk", "Ampa Skywalk, Aminjikarai, Chennai", "+91-44-28343400",
                "ampa@pvr.co.in", 13.0732, 80.2206, 4L, "Chennai", now)
        );

        theatreSearchRepository.saveAll(theatres);
        log.info("Initialized {} theatres", theatres.size());
    }

    private void initializeShows() {
        log.info("Initializing shows data...");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime today = now.toLocalDate().atStartOfDay();
        LocalDateTime tomorrow = today.plusDays(1);

        List<ShowDocument> shows = Arrays.asList(
            // Today's shows - INOX R-City Mall (Theatre 1)
            createShow("1", 1L, "Avengers: Endgame", today.plusHours(10), today.plusHours(13),
                new BigDecimal("350.00"), 180, "SCHEDULED", 1L, "Screen 1", 1L, "INOX R-City Mall", 1L, "Mumbai", now),
            createShow("2", 1L, "Avengers: Endgame", today.plusHours(14), today.plusHours(17),
                new BigDecimal("350.00"), 180, "SCHEDULED", 1L, "Screen 1", 1L, "INOX R-City Mall", 1L, "Mumbai", now),
            createShow("3", 1L, "Avengers: Endgame", today.plusHours(19), today.plusHours(22),
                new BigDecimal("400.00"), 180, "SCHEDULED", 1L, "Screen 1", 1L, "INOX R-City Mall", 1L, "Mumbai", now),

            createShow("4", 2L, "Spider-Man: No Way Home", today.plusHours(11), today.plusHours(14),
                new BigDecimal("300.00"), 150, "SCHEDULED", 2L, "Screen 2", 1L, "INOX R-City Mall", 1L, "Mumbai", now),
            createShow("5", 2L, "Spider-Man: No Way Home", today.plusHours(16), today.plusHours(19),
                new BigDecimal("300.00"), 150, "SCHEDULED", 2L, "Screen 2", 1L, "INOX R-City Mall", 1L, "Mumbai", now),
            createShow("6", 2L, "Spider-Man: No Way Home", today.plusHours(20), today.plusHours(23),
                new BigDecimal("350.00"), 150, "SCHEDULED", 2L, "Screen 2", 1L, "INOX R-City Mall", 1L, "Mumbai", now),

            // PVR Phoenix Mills (Theatre 2)
            createShow("7", 3L, "The Batman", today.plusHours(12), today.plusHours(15),
                new BigDecimal("280.00"), 200, "SCHEDULED", 5L, "Audi 1", 2L, "PVR Phoenix Mills", 1L, "Mumbai", now),
            createShow("8", 3L, "The Batman", today.plusHours(18), today.plusHours(21),
                new BigDecimal("320.00"), 200, "SCHEDULED", 5L, "Audi 1", 2L, "PVR Phoenix Mills", 1L, "Mumbai", now),

            // Cinepolis Fun Republic (Theatre 3)
            createShow("9", 4L, "Doctor Strange 2", today.plusHours(13), today.plusHours(16),
                new BigDecimal("250.00"), 180, "SCHEDULED", 8L, "Screen A", 3L, "Cinepolis Fun Republic", 1L, "Mumbai", now),
            createShow("10", 4L, "Doctor Strange 2", today.plusHours(17), today.plusHours(20),
                new BigDecimal("280.00"), 180, "SCHEDULED", 8L, "Screen A", 3L, "Cinepolis Fun Republic", 1L, "Mumbai", now),

            // Tomorrow's shows
            createShow("11", 1L, "Avengers: Endgame", tomorrow.plusHours(10), tomorrow.plusHours(13),
                new BigDecimal("350.00"), 180, "SCHEDULED", 1L, "Screen 1", 1L, "INOX R-City Mall", 1L, "Mumbai", now),
            createShow("12", 1L, "Avengers: Endgame", tomorrow.plusHours(14), tomorrow.plusHours(17),
                new BigDecimal("350.00"), 180, "SCHEDULED", 1L, "Screen 1", 1L, "INOX R-City Mall", 1L, "Mumbai", now),
            createShow("13", 1L, "Avengers: Endgame", tomorrow.plusHours(19), tomorrow.plusHours(22),
                new BigDecimal("400.00"), 180, "SCHEDULED", 1L, "Screen 1", 1L, "INOX R-City Mall", 1L, "Mumbai", now),

            // PVR Select City Walk (Theatre 4) - Delhi
            createShow("14", 5L, "Top Gun: Maverick", tomorrow.plusHours(11), tomorrow.plusHours(14),
                new BigDecimal("320.00"), 80, "SCHEDULED", 10L, "Gold 1", 4L, "PVR Select City Walk", 2L, "Delhi", now),
            createShow("15", 5L, "Top Gun: Maverick", tomorrow.plusHours(16), tomorrow.plusHours(19),
                new BigDecimal("350.00"), 80, "SCHEDULED", 10L, "Gold 1", 4L, "PVR Select City Walk", 2L, "Delhi", now),

            // INOX Nehru Place (Theatre 5) - Delhi
            createShow("16", 6L, "Jurassic World Dominion", tomorrow.plusHours(12), tomorrow.plusHours(15),
                new BigDecimal("300.00"), 180, "SCHEDULED", 13L, "Screen 1", 5L, "INOX Nehru Place", 2L, "Delhi", now),
            createShow("17", 6L, "Jurassic World Dominion", tomorrow.plusHours(18), tomorrow.plusHours(21),
                new BigDecimal("330.00"), 180, "SCHEDULED", 13L, "Screen 1", 5L, "INOX Nehru Place", 2L, "Delhi", now)
        );

        showSearchRepository.saveAll(shows);
        log.info("Initialized {} shows", shows.size());
    }

    private CityDocument createCity(String id, String name, String state, String country, String zipCode, LocalDateTime now) {
        CityDocument city = new CityDocument();
        city.setId(id);
        city.setName(name);
        city.setState(state);
        city.setCountry(country);
        city.setZipCode(zipCode);
        city.setCreatedAt(now);
        city.setUpdatedAt(now);
        return city;
    }

    private TheatreDocument createTheatre(String id, String name, String address, String phoneNumber,
                                         String email, double latitude, double longitude, Long cityId,
                                         String cityName, LocalDateTime now) {
        TheatreDocument theatre = new TheatreDocument();
        theatre.setId(id);
        theatre.setName(name);
        theatre.setAddress(address);
        theatre.setPhoneNumber(phoneNumber);
        theatre.setEmail(email);
        theatre.setLocation(new GeoPoint(latitude, longitude));
        theatre.setCityId(cityId);
        theatre.setCityName(cityName);
        theatre.setCreatedAt(now);
        theatre.setUpdatedAt(now);
        return theatre;
    }

    private ShowDocument createShow(String id, Long movieId, String movieTitle, LocalDateTime showDateTime,
                                   LocalDateTime endDateTime, BigDecimal price, int availableSeats, String status,
                                   Long screenId, String screenName, Long theatreId, String theatreName,
                                   Long cityId, String cityName, LocalDateTime now) {
        ShowDocument show = new ShowDocument();
        show.setId(id);
        show.setMovieId(movieId);
        show.setMovieTitle(movieTitle);
        show.setShowDateTime(showDateTime);
        show.setEndDateTime(endDateTime);
        show.setPrice(price);
        show.setAvailableSeats(availableSeats);
        show.setStatus(status);
        show.setScreenId(screenId);
        show.setScreenName(screenName);
        show.setTheatreId(theatreId);
        show.setTheatreName(theatreName);
        show.setCityId(cityId);
        show.setCityName(cityName);
        show.setCreatedAt(now);
        show.setUpdatedAt(now);
        return show;
    }
}