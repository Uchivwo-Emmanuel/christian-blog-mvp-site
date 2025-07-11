# Use a base image with JDK (e.g., Java 17)
FROM eclipse-temurin:17-jdk-alpine

# Set working directory inside the container
WORKDIR /app

# Copy Maven wrapper and pom.xml separately to cache dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy the entire project
COPY . .

# Build the project
RUN ./mvnw clean package -DskipTests

# Run the jar file (replace with your actual jar name if needed)
CMD ["java", "-jar", "target/rabbi-0.0.1-SNAPSHOT.jar"]
