package com.moviebooking.gateway.config;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.List;

//@Configuration
public class SwaggerConfig {

    // Commented out to avoid conflicts with application.yml configuration
    // The service URLs are now configured in application.yml under springdoc.swagger-ui.urls
    
    //@Bean
    //@Lazy(false)
    //public List<AbstractSwaggerUiConfigProperties.SwaggerUrl> swaggerUrls() {
    //    List<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls = new ArrayList<>();
    //    
    //    // User Service
    //    urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
    //            "User Service", 
    //            "/user-service/api-docs", 
    //            "user-service"));
    //    
    //    // Movie Service
    //    urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
    //            "Movie Service", 
    //            "/movie-service/api-docs", 
    //            "movie-service"));
    //    
    //    // Theatre Service
    //    urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
    //            "Theatre Service", 
    //            "/theatre-service/api-docs", 
    //            "theatre-service"));
    //    
    //    // Booking Service
    //    urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
    //            "Booking Service", 
    //            "/booking-service/api-docs", 
    //            "booking-service"));
    //    
    //    // Payment Service
    //    urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
    //            "Payment Service", 
    //            "/payment-service/api-docs", 
    //            "payment-service"));
    //    
    //    // Notification Service
    //    urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
    //            "Notification Service", 
    //            "/notification-service/api-docs", 
    //            "notification-service"));
    //    
    //    return urls;
    //}
}