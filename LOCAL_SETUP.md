# Local Development Guide

## Prerequisites Setup

1. Install PostgreSQL:
```bash
brew install postgresql@14
brew services start postgresql@14
```

2. Install Redis:
```bash
brew install redis
brew services start redis
```

3. Install Kafka:
```bash
brew install kafka
brew services start zookeeper
brew services start kafka
```

4. Install Elasticsearch:
```bash
brew install elasticsearch
brew services start elasticsearch
```

## Initial Database Setup

1. Create required databases:
```sql
CREATE DATABASE user_service;
CREATE DATABASE movie_service;
CREATE DATABASE theatre_service;
CREATE DATABASE booking_service;
CREATE DATABASE payment_service;
CREATE DATABASE notification_service;
```

## Service-Specific Setup and Testing

### 1. Discovery Service (Port: 8761)
```bash
cd discovery-service
./mvnw spring-boot:run
```
- Verify: Open http://localhost:8761
- Expected: Eureka dashboard should be accessible

### 2. API Gateway (Port: 8080)
```bash
cd api-gateway
./mvnw spring-boot:run
```
- Test route: http://localhost:8080/actuator/health
- Expected: `{"status": "UP"}`

### 3. User Service (Port: 8081)

```bash
cd user-service
./mvnw spring-boot:run
```

Sample API Calls:
```bash
# Register User
curl -X POST http://localhost:8080/api/v1/auth/register \
-H "Content-Type: application/json" \
-d '{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
-H "Content-Type: application/json" \
-d '{
  "email": "user@example.com",
  "password": "password123"
}'
```

### 4. Movie Service (Port: 8082)

```bash
cd movie-service
./mvnw spring-boot:run
```

Sample API Calls:
```bash
# Create Movie
curl -X POST http://localhost:8080/api/v1/movies \
-H "Authorization: Bearer YOUR_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
  "title": "Inception",
  "description": "A thief who steals corporate secrets...",
  "duration": 148,
  "genre": "SCIFI",
  "language": "English",
  "releaseDate": "2024-01-01"
}'

# Search Movies
curl -X GET "http://localhost:8080/api/v1/movies/search?query=inception"

# Get Movie Details
curl -X GET http://localhost:8080/api/v1/movies/{movieId}
```

### 5. Theatre Service (Port: 8083)

```bash
cd theatre-service
./mvnw spring-boot:run
```

Sample API Calls:
```bash
# Create Theatre
curl -X POST http://localhost:8080/api/v1/theatres \
-H "Authorization: Bearer YOUR_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
  "name": "PVR Cinemas",
  "location": "Mumbai",
  "address": "123 Main St",
  "totalScreens": 4
}'

# Create Show
curl -X POST http://localhost:8080/api/v1/shows \
-H "Authorization: Bearer YOUR_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
  "movieId": "movie_id",
  "theatreId": "theatre_id",
  "screenId": "screen_id",
  "showTime": "2024-01-01T18:00:00",
  "price": 200.00
}'

# Get Theatre Shows
curl -X GET http://localhost:8080/api/v1/theatres/{theatreId}/shows
```

### 6. Booking Service (Port: 8084)

```bash
cd booking-service
./mvnw spring-boot:run
```

Sample API Calls:
```bash
# Create Booking
curl -X POST http://localhost:8080/api/v1/bookings \
-H "Authorization: Bearer YOUR_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
  "showId": "show_id",
  "seats": ["A1", "A2"],
  "totalAmount": 400.00
}'

# Get Booking Details
curl -X GET http://localhost:8080/api/v1/bookings/{bookingId} \
-H "Authorization: Bearer YOUR_JWT_TOKEN"

# Cancel Booking
curl -X PUT http://localhost:8080/api/v1/bookings/{bookingId}/cancel \
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 7. Payment Service (Port: 8085)

```bash
cd payment-service
./mvnw spring-boot:run
```

Sample API Calls:
```bash
# Process Payment
curl -X POST http://localhost:8080/api/v1/payments \
-H "Authorization: Bearer YOUR_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
  "bookingId": "booking_id",
  "amount": 400.00,
  "paymentMethod": "CREDIT_CARD",
  "cardNumber": "4111111111111111",
  "expiryMonth": "12",
  "expiryYear": "2025",
  "cvv": "123"
}'

# Get Payment Status
curl -X GET http://localhost:8080/api/v1/payments/{paymentId} \
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 8. Notification Service (Port: 8086)

```bash
cd notification-service
./mvnw spring-boot:run
```

Sample API Calls:
```bash
# Send Manual Notification
curl -X POST http://localhost:8080/api/v1/notifications \
-H "Authorization: Bearer YOUR_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
  "userId": "user_id",
  "type": "EMAIL",
  "subject": "Booking Confirmation",
  "templateName": "booking-confirmation",
  "templateData": {
    "userName": "John Doe",
    "movieTitle": "Inception",
    "showTime": "2024-01-01 18:00"
  }
}'

# Get User Notifications
curl -X GET http://localhost:8080/api/v1/notifications/user/{userId} \
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Key Configuration Files

Each service has its own `application.yml` in `src/main/resources`. Key configurations to check:

1. Database connections
2. Kafka topics
3. Redis configuration
4. Email settings (for notification service)
5. JWT secret key
6. Service discovery URLs

## Common Issues and Solutions

1. Service Discovery Issues:
- Check if Eureka is running
- Verify service registration in Eureka dashboard
- Check network connectivity between services

2. Database Connection:
- Verify PostgreSQL is running
- Check database exists
- Validate credentials

3. Kafka Connection:
- Ensure Zookeeper and Kafka are running
- Verify topic creation
- Check consumer group configurations

4. Redis Connection:
- Verify Redis is running
- Check Redis port (default 6379)
- Test connection using redis-cli

## Health Checks

Monitor service health:
```bash
# For each service
curl -X GET http://localhost:{PORT}/actuator/health

# Overall system health via API Gateway
curl -X GET http://localhost:8080/actuator/health
```

## Logging

Each service logs to `logs/` directory. Monitor logs:
```bash
# Tail logs for any service
tail -f {service-name}/logs/application.log
```

## Testing Flow

1. Register a user
2. Login to get JWT token
3. Create a movie
4. Create a theatre and shows
5. Make a booking
6. Process payment
7. Check notifications

Keep the JWT token from login for subsequent requests.
