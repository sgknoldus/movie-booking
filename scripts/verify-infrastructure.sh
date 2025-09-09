#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Checking infrastructure services...${NC}\n"

# Function to print status
print_status() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ $1 is healthy${NC}"
    else
        echo -e "${RED}✗ $1 is not healthy${NC}"
    fi
}

# Check PostgreSQL
echo "Checking PostgreSQL..."
if docker exec movie-booking-postgres pg_isready -U postgres > /dev/null 2>&1; then
    echo -e "${GREEN}✓ PostgreSQL is running${NC}"
    echo "Databases:"
    docker exec -it movie-booking-postgres psql -U postgres -c "\l" | grep service
else
    echo -e "${RED}✗ PostgreSQL is not running${NC}"
fi
echo

# Check Redis
echo "Checking Redis..."
if docker exec movie-booking-system-redis-1 redis-cli ping | grep -q "PONG"; then
    echo -e "${GREEN}✓ Redis is running${NC}"
    docker exec movie-booking-system-redis-1 redis-cli info | grep "redis_version"
else
    echo -e "${RED}✗ Redis is not running${NC}"
fi
echo

# Check Elasticsearch
echo "Checking Elasticsearch..."
if curl -s "http://localhost:9200/_cluster/health" | grep -q "green\|yellow"; then
    echo -e "${GREEN}✓ Elasticsearch is running${NC}"
    curl -s "http://localhost:9200/_cluster/health?pretty" | grep "status"
else
    echo -e "${RED}✗ Elasticsearch is not running${NC}"
fi
echo

# Check Zookeeper
echo "Checking Zookeeper..."
if echo stat | nc localhost 2181 | grep -q "Mode:"; then
    echo -e "${GREEN}✓ Zookeeper is running${NC}"
else
    echo -e "${RED}✗ Zookeeper is not running${NC}"
fi
echo

# Check Kafka
echo "Checking Kafka..."
if docker exec movie-booking-kafka kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Kafka is running${NC}"
    echo "Topics:"
    docker exec movie-booking-kafka kafka-topics --bootstrap-server localhost:9092 --list
else
    echo -e "${RED}✗ Kafka is not running${NC}"
fi
echo

# Show all container statuses
echo -e "${YELLOW}Container Status:${NC}"
docker-compose ps

echo -e "\n${YELLOW}Container Logs (last 5 lines each):${NC}"
for service in postgres redis elasticsearch zookeeper kafka; do
    echo -e "\n${YELLOW}$service logs:${NC}"
    docker-compose logs --tail=5 $service
done
