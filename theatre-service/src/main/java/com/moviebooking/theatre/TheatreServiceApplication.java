package com.moviebooking.theatre;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableKafka
@EnableTransactionManagement
@OpenAPIDefinition(
    info = @Info(
        title = "Theatre Service API",
        version = "1.0",
        description = "API for managing theatres, cities, screens and movie shows. Requires JWT token from User Service."
    )
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "Enter JWT token obtained from User Service /api/auth/login endpoint"
)
public class TheatreServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TheatreServiceApplication.class, args);
    }
}