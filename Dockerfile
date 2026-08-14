# Step 1: Build stage avec JDK 17
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copier le wrapper et les fichiers de configuration Gradle
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./

# Rendre le script gradlew exécutable
RUN chmod +x ./gradlew

# Telecharger les dépendances
RUN ./gradlew dependencies --no-daemon

# Copier le code source et construire le JAR
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# Step 2: Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]