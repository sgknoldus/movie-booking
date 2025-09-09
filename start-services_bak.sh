#!/bin/bash

# Start infrastructure services
echo "Starting infrastructure services..."
docker-compose up -d postgres kafka redis elasticsearch

# Wait for infrastructure services to be ready
echo "Waiting for infrastructure services to be ready..."
sleep 30

# Start microservices in sequence
echo "Starting Discovery Service..."
cd discovery-service && ./mvnw spring-boot:run &
sleep 30  # Wait for Eureka to be ready

echo "Starting API Gateway..."
cd ../api-gateway && ./mvnw spring-boot:run &
sleep 10

echo "Starting User Service..."
cd ../user-service && ./mvnw spring-boot:run &
sleep 10

echo "Starting Movie Service..."
cd ../movie-service && ./mvnw spring-boot:run &
sleep 10

echo "Starting Theatre Service..."
cd ../theatre-service && ./mvnw spring-boot:run &
sleep 10

echo "Starting Booking Service..."
cd ../booking-service && ./mvnw spring-boot:run &
sleep 10

echo "Starting Payment Service..."
cd ../payment-service && ./mvnw spring-boot:run &
sleep 10

echo "Starting Notification Service..."
cd ../notification-service && ./mvnw spring-boot:run &

echo "All services started. You can check service status at http://localhost:8761"
