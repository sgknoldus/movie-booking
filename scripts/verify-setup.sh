#!/bin/bash

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color
YELLOW='\033[1;33m'

echo -e "${YELLOW}Starting verification of movie booking system infrastructure...${NC}\n"

# Function to check if a service is healthy
check_service() {
    local service=$1
    local container_name=$2
    local check_command=$3
    
    echo -e "Checking ${service}..."
    if docker ps | grep -q "${container_name}"; then
        if eval "${check_command}"; then
            echo -e "${GREEN}✓ ${service} is running and healthy${NC}"
            return 0
        else
            echo -e "${RED}✗ ${service} is running but may not be healthy${NC}"
            return 1
        fi
    else
        echo -e "${RED}✗ ${service} is not running${NC}"
        return 1
    fi
}

# Stop containers and clean Kafka/Zookeeper (to avoid cluster ID issues)
# but preserve database volumes unless --reset flag is used
echo "Stopping all existing containers..."
docker-compose down

# Always clean Kafka and Zookeeper to avoid cluster ID conflicts
echo "Cleaning Kafka and Zookeeper data to prevent cluster ID conflicts..."
docker volume rm movie-booking-system_kafka_data 2>/dev/null || true

# Only remove all volumes if --reset flag is used
if [ "$1" = "--reset" ]; then
    echo "Removing all volumes..."
    docker volume rm movie-booking-system_postgres_data 2>/dev/null || true
    docker volume rm movie-booking-system_redis_data 2>/dev/null || true
    docker volume rm movie-booking-system_es_data 2>/dev/null || true
fi


# Start all services
echo -e "\nStarting all services..."
docker-compose up -d postgres redis elasticsearch zookeeper kafka

# Wait for services to start
echo "Waiting 30 seconds for services to initialize..."
sleep 30

# Setup required databases and topics
echo -e "\nSetting up databases and topics..."

# Create theatre_service database if it doesn't exist
docker exec movie-booking-postgres psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = 'theatre_service'" | grep -q 1 || docker exec movie-booking-postgres psql -U postgres -c "CREATE DATABASE theatre_service;"

# Create required Kafka topics if they don't exist
topics=("booking-confirmed" "booking-cancelled" "payment-completed" "theatre-events")
for topic in "${topics[@]}"; do
    if ! docker exec movie-booking-kafka kafka-topics --bootstrap-server localhost:9092 --list | grep -q "^${topic}$"; then
        echo "Creating topic: $topic"
        docker exec movie-booking-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic "$topic" --partitions 3 --replication-factor 1
    fi
done

echo -e "\nVerifying services:\n"

# Check PostgreSQL
check_service "PostgreSQL" "movie-booking-postgres" "docker exec movie-booking-postgres pg_isready -U postgres && echo 'Databases:' && docker exec movie-booking-postgres psql -U postgres -l" || exit_status=1

echo ""

# Check Redis
check_service "Redis" "movie-booking-system-redis" "docker exec \$(docker ps -q -f name=redis) redis-cli ping | grep -q 'PONG' && echo 'Redis version:' && docker exec \$(docker ps -q -f name=redis) redis-cli info server | grep redis_version" || exit_status=1

echo ""

# Check Elasticsearch
check_service "Elasticsearch" "movie-booking-system-elasticsearch" "curl -s localhost:9200/_cluster/health | grep -q '\"status\":\"green\"' && echo 'Cluster Status:' && curl -s localhost:9200/_cluster/health | grep status" || exit_status=1

echo ""

# Check Zookeeper
check_service "Zookeeper" "movie-booking-system-zookeeper" "docker exec \$(docker ps -q -f name=zookeeper) bash -c 'echo srvr | nc localhost 2181 | grep -q \"Mode: \"'" || exit_status=1

echo ""

# Check Kafka
check_service "Kafka" "movie-booking-kafka" "docker exec movie-booking-kafka kafka-topics --bootstrap-server localhost:9092 --list && echo 'Topics created:' && docker exec movie-booking-kafka kafka-topics --bootstrap-server localhost:9092 --list | wc -l" || exit_status=1

echo -e "\n${YELLOW}Container Status:${NC}"
docker-compose ps

echo -e "\n${YELLOW}Container Logs (last 5 lines each):${NC}"
for service in postgres redis elasticsearch zookeeper kafka; do
    echo -e "\n${YELLOW}${service} logs:${NC}"
    docker-compose logs --tail=5 $service
done

if [ "$exit_status" == "1" ]; then
    echo -e "\n${RED}Some services failed the health check. Please check the logs above for details.${NC}"
    exit 1
else
    echo -e "\n${GREEN}All services are running successfully!${NC}"
    exit 0
fi
