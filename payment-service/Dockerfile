# Multi-stage build for payment-service
FROM maven:3.9.5-openjdk-17-slim as builder

# Set working directory
WORKDIR /app

# Copy parent pom.xml and common-lib first for better layer caching
COPY pom.xml ./
COPY common-lib/ ./common-lib/

# Copy payment-service specific files
COPY payment-service/pom.xml ./payment-service/
COPY payment-service/src ./payment-service/src

# Build common-lib first
RUN mvn -f common-lib/pom.xml clean install -DskipTests -B

# Download dependencies and build the application
RUN mvn -f payment-service/pom.xml dependency:go-offline -B
RUN mvn -f payment-service/pom.xml clean package -DskipTests -B

# Production stage
FROM openjdk:17-jre-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/payment-service/target/*.jar app.jar

# Change ownership to non-root user
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8085

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8085/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]