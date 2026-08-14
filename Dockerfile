# Step 1: Build app w/ gradle
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy Gradle configuration files to optimize dependency caching
COPY build.gradle.kts settings.gradle.kts gradle/ ./
COPY src ./src

COPY . .

# Build and create the JAR file (skipping tests during image build)
RUN gradle bootJar --no-daemon -x test

# Step 2: Minimal runtime image with JDK 17
#FROM eclipse-temurin:17-jre-alpine

# Step 2: Runtime image (Compatible with Mac M1/M2/M3 and Linux x86)
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the compiled JAR from the previous stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose API port
EXPOSE 8080

# Launch command
ENTRYPOINT ["java", "-jar", "app.jar"]