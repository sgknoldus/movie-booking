# Multi-stage build for discovery-service
FROM openjdk:17-jdk-slim as builder

# Set working directory
WORKDIR /app

# Copy parent pom.xml and common-lib first for better layer caching
COPY pom.xml ./
COPY common-lib/ ./common-lib/

# Copy discovery-service specific files
COPY discovery-service/pom.xml ./discovery-service/
COPY discovery-service/src ./discovery-service/src

# Download dependencies and build the application
RUN ./mvnw -f discovery-service/pom.xml dependency:go-offline -B
RUN ./mvnw -f discovery-service/pom.xml clean package -DskipTests -B

# Production stage
FROM openjdk:17-jre-slim

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/discovery-service/target/*.jar app.jar

# Change ownership to non-root user
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8761

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8761/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]