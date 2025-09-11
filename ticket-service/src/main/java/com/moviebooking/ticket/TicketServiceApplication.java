package com.moviebooking.ticket;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
@OpenAPIDefinition(
    info = @Info(
        title = "Ticket Service API",
        version = "1.0",
        description = "API for managing movie tickets and booking confirmations"
    )
)
public class TicketServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TicketServiceApplication.class, args);
    }
}