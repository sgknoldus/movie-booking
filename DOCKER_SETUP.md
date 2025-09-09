# Docker-based Setup Guide

This guide explains how to set up the Movie Booking System using Docker containers.

## Prerequisites

1. Install Docker:
```bash
# macOS (using Homebrew)
brew install --cask docker

# Linux
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
```

2. Install Docker Compose:
```bash
# macOS (included with Docker Desktop)
# Linux
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

## Building and Running

1. Clone the repository:
```bash
git clone <repository-url>
cd movie-booking-system
```

2. Set up Docker network:
```bash
# Create network for service communication
docker network create movie-booking-network
```

3. Clean up any existing containers (if needed):
```bash
# Stop and remove all containers, networks, and volumes
docker-compose down -v

# Remove all images
docker-compose down --rmi all

# Prune unused resources
docker system prune -a
```

4. Start services in the correct order:

a. Start infrastructure services first:
```bash
# Build and start infrastructure services
docker-compose up -d postgres redis elasticsearch zookeeper kafka

# Wait for infrastructure services to be healthy
sleep 30

# Verify infrastructure services
docker-compose ps
docker-compose logs postgres kafka elasticsearch redis
```

b. Start the Discovery Service:
```bash
# Build and start discovery service
docker-compose up -d discovery-service

# Wait for discovery service to be healthy
sleep 20

# Verify Eureka is running
curl http://localhost:8761/actuator/health
```

c. Start core services:
```bash
# Build and start main services
docker-compose up -d api-gateway user-service movie-service theatre-service

# Wait for services to register
sleep 30

# Verify services in Eureka
curl http://localhost:8761/eureka/apps
```

d. Start dependent services:
```bash
# Build and start services that depend on core services
docker-compose up -d booking-service payment-service notification-service

# Wait for services to register
sleep 20
```

e. Start monitoring:
```bash
# Start monitoring stack
docker-compose up -d prometheus grafana
```

5. Verify the setup:
```bash
# Check all container statuses
docker-compose ps

# Check service health
for service in discovery-service api-gateway user-service movie-service theatre-service booking-service payment-service notification-service; do
    echo "Checking $service..."
    curl -s http://localhost:8761/eureka/apps/$service
done

# Check Grafana
curl http://localhost:3000
```

6. Monitor the logs:
```bash
# Watch all logs
docker-compose logs -f

# Watch specific service logs
docker-compose logs -f service-name
```

7. If you encounter issues:
```bash
# Rebuild specific service
docker-compose build service-name
docker-compose up -d --no-deps service-name

# Check service logs
docker-compose logs --tail=100 service-name

# Restart service
docker-compose restart service-name
```

## Service URLs and Ports

After starting the services, they will be available at:

1. Infrastructure Services:
   - PostgreSQL: localhost:5432
   - Redis: localhost:6379
   - Elasticsearch: localhost:9200
   - Kafka: localhost:9092
   - Zookeeper: localhost:2181

2. Application Services:
   - Discovery Service (Eureka): http://localhost:8761
   - API Gateway: http://localhost:8080
   - User Service: http://localhost:8081
   - Movie Service: http://localhost:8082
   - Theatre Service: http://localhost:8083
   - Booking Service: http://localhost:8084
   - Payment Service: http://localhost:8085
   - Notification Service: http://localhost:8086

3. Monitoring:
   - Prometheus: http://localhost:9090
   - Grafana: http://localhost:3000 (admin/admin123)

## Health Checks

Check service health:
```bash
# Check all service statuses
docker-compose ps

# Check specific service health
docker inspect --format='{{json .State.Health.Status}}' movie-booking-discovery
```

## Common Docker Commands

1. Service Management:
```bash
# Stop all services
docker-compose down

# Restart a specific service
docker-compose restart service-name

# View logs for a specific service
docker-compose logs -f service-name

# Scale a service
docker-compose up -d --scale service-name=2
```

2. Database Management:
```bash
# Connect to PostgreSQL
docker exec -it movie-booking-postgres psql -U postgres

# Connect to Redis
docker exec -it movie-booking-redis redis-cli

# Connect to Elasticsearch
curl http://localhost:9200
```

3. Kafka Operations:
```bash
# List topics
docker exec -it movie-booking-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Create a topic
docker exec -it movie-booking-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic my-topic --partitions 1 --replication-factor 1
```

## Troubleshooting

1. Container won't start:
```bash
# Check logs
docker-compose logs service-name

# Check container status
docker ps -a
```

2. Service discovery issues:
```bash
# Check Eureka status
curl http://localhost:8761/actuator/health

# View Eureka logs
docker-compose logs discovery-service
```

3. Database connection issues:
```bash
# Check PostgreSQL logs
docker-compose logs postgres

# Verify database existence
docker exec -it movie-booking-postgres psql -U postgres -c "\l"
```

4. Reset everything:
```bash
# Stop and remove all containers, networks, and volumes
docker-compose down -v

# Remove all images
docker-compose down --rmi all

# Start fresh
docker-compose up -d
```

## Development Workflow

1. Making changes to a service:
```bash
# Rebuild specific service
docker-compose build service-name

# Restart service with new changes
docker-compose up -d --no-deps service-name
```

2. Monitoring changes:
```bash
# Watch service logs
docker-compose logs -f service-name

# Check service health
curl http://localhost:8080/actuator/health
```

## Data Persistence

Docker volumes are used for data persistence:
- postgres_data: PostgreSQL data
- redis_data: Redis data
- elasticsearch_data: Elasticsearch data
- prometheus_data: Prometheus metrics
- grafana_data: Grafana dashboards

To manage volumes:
```bash
# List volumes
docker volume ls

# Inspect volume
docker volume inspect volume_name

# Clean up volumes
docker volume prune
```

## Security Notes

1. Default credentials (for development only):
   - PostgreSQL: postgres/postgres
   - Grafana: admin/admin123

2. For production:
   - Change all default passwords
   - Use Docker secrets or environment files
   - Enable SSL/TLS
   - Configure proper network isolation

## Performance Tuning

1. Container Resource Limits:
   - Set appropriate memory limits
   - Monitor CPU usage
   - Adjust JVM parameters

2. Volume Performance:
   - Use named volumes
   - Consider bind mounts for development
   - Monitor I/O performance

Remember to never use these configurations in production without proper security hardening!
