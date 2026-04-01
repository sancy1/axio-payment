# ============================================
# Multi-stage Dockerfile for Payment Service
# Stage 1: Build
# Stage 2: Runtime
# ============================================

# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (for better caching)
COPY pom.xml .
RUN mvn dependency:resolve

# Copy the entire project
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Create a non-root user for security
RUN useradd -m -u 1000 appuser

# Copy the built JAR from builder stage
COPY --from=builder /app/target/payment-service-1.0.0.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

USER appuser

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD java -cp app.jar org.springframework.boot.loader.JarLauncher \
  -Dspring.boot.actuate.endpoint.web.enabled=true \
  -Dmanagement.endpoints.web.exposure.include=health || exit 1

# Environment variables (will be overridden by docker-compose or Render)
ENV JAVA_OPTS="-Xmx256m -Xms128m"

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
