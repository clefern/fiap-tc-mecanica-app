# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Grupo 14SOAT - FIAP"
LABEL version="0.0.1"
LABEL description="Mecânica API - Sistema de Gestão de Oficina"

WORKDIR /app

# Install netcat for entrypoint script and curl for healthcheck
RUN apk add --no-cache netcat-openbsd curl

# Copy jar from builder
COPY --from=builder /app/target/*.jar app.jar

# Copy entrypoint script
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Environment variables with defaults (can be overridden)
ENV DB_HOST=postgres \
    DB_PORT=5432 \
    DB_NAME=mecanica \
    DB_USER=mecanica_user \
    DB_PASSWORD=mecanica_pass \
    SERVER_PORT=8080 \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Expose port
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["/app/entrypoint.sh"]
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
