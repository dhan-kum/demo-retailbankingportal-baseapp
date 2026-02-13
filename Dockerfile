#
# Build stage
#
FROM maven:3.9-eclipse-temurin-21 AS build
COPY src /home/app/src
COPY pom.xml /home/app
COPY package.json webpack.config.js /home/app/
WORKDIR /home/app
RUN mvn -f /home/app/pom.xml clean install -DskipTests=true

#
# Package stage - Using stable eclipse-temurin JRE instead of EA JDK
#
FROM eclipse-temurin:21-jre-jammy

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser -u 10001 appuser

# Set working directory
WORKDIR /app

# Copy application files with proper ownership
COPY --from=build --chown=appuser:appuser /home/app/target/baseapp-0.0.1-SNAPSHOT.jar /app/app.jar

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD java -cp /app/app.jar org.springframework.boot.actuator.health.HealthEndpoint || exit 1

EXPOSE 8080

# Use exec form for proper signal handling and add security-hardened JVM options
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}", \
    "-jar", \
    "/app/app.jar"]

