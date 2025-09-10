package com.moviebooking.theatre.application.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TheatreResponse {
    private UUID id;
    private String name;
    private String city;
    private String address;
    private int totalScreens;
}