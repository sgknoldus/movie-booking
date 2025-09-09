package com.moviebooking.user.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String type;
    private String email;
    private String name;
}
