package com.moviebooking.theatre.application.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TheatreSearchResponse {
    private UUID theatreId;
    private String name;
    private String city;
    private String address;
    private List<ShowInfo> shows;

    @Data
    @Builder
    public static class ShowInfo {
        private UUID showId;
        private String showTime;
        private String screenNumber;
        private List<String> availableSeats;
        private double price;
    }
}
