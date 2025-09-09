#!/bin/bash

echo "Starting Movie Booking System..."

# Check if network exists
if ! docker network ls | grep -q movie-booking-system; then
    echo "Creating Docker network: movie-booking-network..."
    docker network create movie-booking-network
else
    echo "Docker network movie-booking-network already exists"
fi

# Function to check service health
check_service_health() {
    local service=$1
    local port=$2
    local endpoint=${3:-actuator/health}
    local max_attempts=30
    local attempt=1

    echo "Checking $service health..."
    while [ $attempt -le $max_attempts ]; do
        if curl -s "http://localhost:$port/$endpoint" | grep -q "UP"; then
            echo "$service is healthy"
            return 0
        fi
        echo "Attempt $attempt: $service not healthy yet..."
        attempt=$((attempt + 1))
        sleep 5
    done
    echo "$service failed to become healthy"
    return 1
}

# Start infrastructure services
echo "Starting infrastructure services..."
#docker-compose up -d postgres redis elasticsearch zookeeper kafka

# Wait for infrastructure
echo "Waiting for infrastructure services..."
#sleep 30

# Start Discovery Service
echo "Starting Discovery Service..."
docker-compose up -d discovery-service
check_service_health "Discovery Service" 8761 || exit 1

# Start API Gateway
echo "Starting API Gateway..."
docker-compose up -d api-gateway
check_service_health "API Gateway" 8080 || exit 1

# Start core services
echo "Starting core services..."
docker-compose up -d user-service movie-service theatre-service
sleep 30

# Start dependent services
echo "Starting dependent services..."
docker-compose up -d booking-service payment-service notification-service
sleep 20

# Start monitoring
echo "Starting monitoring services..."
docker-compose up -d prometheus grafana

echo "All services started. Checking status..."
docker-compose ps

echo "
Services are available at:
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin123)

To monitor logs:
docker-compose logs -f
"
