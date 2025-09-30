# Multi-stage build for optimized image size
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy Maven configuration files
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:resolve

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds in development)
RUN ./mvnw clean package -DskipTests

# Production stage
FROM eclipse-temurin:17-jre-alpine AS production

# Add a non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the JAR file from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

# Expose port
EXPOSE 8083

# JVM optimizations for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]