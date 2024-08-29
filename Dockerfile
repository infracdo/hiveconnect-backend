# First stage: Build the JAR file
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the project files into the container
COPY . .

# Run Maven to build the project and create the JAR file
RUN mvn clean package -DskipTests

# Second stage: Create the final image with the JAR file
FROM openjdk:17-jdk-alpine
WORKDIR /app

# Copy the JAR file from the first stage
COPY --from=build /app/target/autoprov-0.0.1-SNAPSHOT.jar /app/autoprov-0.0.1-SNAPSHOT.jar

# Expose the application port
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "/app/autoprov-0.0.1-SNAPSHOT.jar"]
