# Step 1: Build the Spring Boot app using Gradle
FROM gradle:7.5-jdk17 AS build

# Set the working directory for Gradle build
WORKDIR /app

# Copy the Gradle wrapper and build files first
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# Step 2: Copy the rest of the project files
COPY . .

# Step 3: Give execute permission to gradlew
RUN chmod +x gradlew

# Step 4: Download the Gradle dependencies
RUN ./gradlew --no-daemon dependencies

# Step 5: Build the Spring Boot jar file
RUN ./gradlew --no-daemon clean build -x test

# Step 6: Create a smaller runtime image to run the app
FROM openjdk:17-jdk-slim AS runtime

# Set the working directory for the runtime
WORKDIR /app

# Copy the Spring Boot jar from the build stage
COPY --from=build /app/build/libs/user-server-*-SNAPSHOT.jar /app/app.jar

# Expose the port your Spring Boot application runs on
EXPOSE 4000
ENV SPRING_CONFIG_IMPORT=configserver:http://localhost:8888
ENV EUREKA_SERVER_URL=http://localhost:9999/eureka
ENV GOOGLE_CLIENT_ID=your-client-id
ENV GOOGLE_CLIENT_SECRET=your-client-secret
ENV GITHUB_CLIENT_ID=your-client-id
ENV GITHUB_CLIENT_SECRET=your-client-secret
ENV DB_HOST=localhost
ENV DB_USERNAME=postgres
ENV DB_PASSWORD=password
# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
