#!/bin/bash

echo "Verifying Movie Booking System Services..."

# Function to check service health
check_service_health() {
    local service=$1
    local port=$2
    local endpoint=${3:-actuator/health}
    
    echo "Checking $service health..."
    if curl -s "http://localhost:$port/$endpoint" | grep -q "UP"; then
        echo "✅ $service is healthy"
        return 0
    else
        echo "❌ $service is not healthy"
        return 1
    fi
}

# Function to check Docker container status
check_container_status() {
    local container=$1
    if docker ps | grep -q "$container"; then
        echo "✅ $container is running"
        return 0
    else
        echo "❌ $container is not running"
        return 1
    fi
}

# Initialize counters
total_services=0
healthy_services=0

echo "Checking Docker network..."
if docker network ls | grep -q movie-booking-system; then
    echo "✅ Docker network movie-booking-network exists"
else
    echo "❌ Docker network movie-booking-network does not exist"
    exit 1
fi

echo -e "\nChecking Infrastructure Services..."
total_services=$((total_services + 4))
check_container_status "postgres" && healthy_services=$((healthy_services + 1))
check_container_status "redis" && healthy_services=$((healthy_services + 1))
check_container_status "elasticsearch" && healthy_services=$((healthy_services + 1))
check_container_status "kafka" && healthy_services=$((healthy_services + 1))

echo -e "\nChecking Core Services..."
total_services=$((total_services + 2))
check_service_health "Discovery Service" 8761 && healthy_services=$((healthy_services + 1))
check_service_health "API Gateway" 8080 && healthy_services=$((healthy_services + 1))

echo -e "\nChecking Business Services..."
total_services=$((total_services + 6))
check_service_health "User Service" 8081 && healthy_services=$((healthy_services + 1))
check_service_health "Movie Service" 8082 && healthy_services=$((healthy_services + 1))
check_service_health "Theatre Service" 8083 && healthy_services=$((healthy_services + 1))
check_service_health "Booking Service" 8084 && healthy_services=$((healthy_services + 1))
check_service_health "Payment Service" 8085 && healthy_services=$((healthy_services + 1))
check_service_health "Notification Service" 8086 && healthy_services=$((healthy_services + 1))

echo -e "\nChecking Monitoring Services..."
total_services=$((total_services + 2))
check_container_status "prometheus" && healthy_services=$((healthy_services + 1))
check_container_status "grafana" && healthy_services=$((healthy_services + 1))

echo -e "\nService Health Summary:"
echo "Healthy Services: $healthy_services/$total_services"

if [ $healthy_services -eq $total_services ]; then
    echo "✅ All services are healthy!"
    exit 0
else
    echo "❌ Some services are not healthy. Please check the logs for more details."
    echo "You can check individual service logs using: docker-compose logs <service-name>"
    exit 1
fi
