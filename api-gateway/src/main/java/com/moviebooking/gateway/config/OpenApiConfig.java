package com.moviebooking.gateway.config;

import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class OpenApiConfig {

    @Bean
    @Primary
    public SwaggerUiConfigProperties swaggerUiConfigProperties() {
        SwaggerUiConfigProperties properties = new SwaggerUiConfigProperties();
        
        // Set the configuration URL
        properties.setConfigUrl("/api-docs/swagger-config");
        
        // Configure URLs for all services
        Set<SwaggerUiConfigProperties.SwaggerUrl> urls = new HashSet<>();
        urls.add(new SwaggerUiConfigProperties.SwaggerUrl("User Service", "/user-service/api-docs", "user-service"));
        urls.add(new SwaggerUiConfigProperties.SwaggerUrl("Movie Service", "/movie-service/api-docs", "movie-service"));
        urls.add(new SwaggerUiConfigProperties.SwaggerUrl("Theatre Service", "/theatre-service/api-docs", "theatre-service"));
        urls.add(new SwaggerUiConfigProperties.SwaggerUrl("Booking Service", "/booking-service/api-docs", "booking-service"));
        urls.add(new SwaggerUiConfigProperties.SwaggerUrl("Payment Service", "/payment-service/api-docs", "payment-service"));
        urls.add(new SwaggerUiConfigProperties.SwaggerUrl("Notification Service", "/notification-service/api-docs", "notification-service"));
        
        properties.setUrls(urls);
        
        // Disable the default URL to force using the dropdown
        properties.setUrl(null);
        
        return properties;
    }
}