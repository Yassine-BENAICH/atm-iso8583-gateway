# --- Stage 1: Build the application ---
FROM maven:3.8-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Run the application ---
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# The build stage creates a fat JAR
COPY --from=builder /app/target/*.jar app.jar

# Expose Gateway REST Port (8080) and Mock Switch Port (9000)
EXPOSE 8080
EXPOSE 9000

# Default entrypoint for Gateway app
ENTRYPOINT ["java", "-jar", "app.jar"]
