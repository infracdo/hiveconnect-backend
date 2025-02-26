# First stage: Build the JAR file
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and .env to cache dependencies
COPY pom.xml .env ./

# Cache dependencies for faster builds
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Build the JAR file
RUN mvn clean package -DskipTests

# Second stage: Create the final image
FROM openjdk:17-jdk-alpine
WORKDIR /app

# Copy the built JAR and .env file from the build stage
COPY --from=build /app/target/autoprov-0.0.1-SNAPSHOT.jar /app/autoprov-0.0.1-SNAPSHOT.jar
COPY --from=build /app/.env /app/.env

# Ensure .env is present in the final image
RUN ls -la /app

# Expose the application port
EXPOSE 8080

# Load environment variables from the .env file and run the JAR
ENTRYPOINT ["/bin/sh", "-c", "set -a && . /app/.env && exec java -jar /app/autoprov-0.0.1-SNAPSHOT.jar"]
