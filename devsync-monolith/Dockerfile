# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /app

# Copy maven executable to the image
COPY mvnw .
COPY .mvn .mvn

# Copy the pom.xml files
COPY pom.xml .
COPY devsync-common/pom.xml devsync-common/
COPY devsync-monolith/pom.xml devsync-monolith/

# Resolve dependencies (caching step)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY devsync-common/src devsync-common/src
COPY devsync-monolith/src devsync-monolith/src

# Build the application
RUN ./mvnw clean package -DskipTests -pl devsync-monolith -am

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built artifact from the builder stage
COPY --from=builder /app/devsync-monolith/target/*.jar app.jar

# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose the application port
EXPOSE 8080

# Set entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]
