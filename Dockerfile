# Use an official OpenJDK runtime as a base image for building the application

# Use an official OpenJDK runtime as a base image for running the application
FROM eclipse-temurin:23-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file from the local target directory into the container
COPY target/eatsadvisor-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
