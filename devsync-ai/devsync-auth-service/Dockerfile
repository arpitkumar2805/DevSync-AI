FROM maven:3.9.8-eclipse-temurin-21-alpine as builder
WORKDIR /app
COPY pom.xml .
COPY devsync-common/pom.xml devsync-common/
COPY devsync-monolith/pom.xml devsync-monolith/
COPY devsync-auth-service/pom.xml devsync-auth-service/
COPY devsync-org-user-service/pom.xml devsync-org-user-service/
COPY devsync-project-service/pom.xml devsync-project-service/
COPY devsync-task-service/pom.xml devsync-task-service/
COPY devsync-notification-service/pom.xml devsync-notification-service/
COPY devsync-ai-service/pom.xml devsync-ai-service/
COPY devsync-discovery-server/pom.xml devsync-discovery-server/
COPY devsync-config-server/pom.xml devsync-config-server/
COPY devsync-api-gateway/pom.xml devsync-api-gateway/
RUN mvn dependency:go-offline -B -pl devsync-auth-service -am
COPY devsync-common/src devsync-common/src
COPY devsync-auth-service/src devsync-auth-service/src
RUN mvn clean package -DskipTests -pl devsync-auth-service -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/devsync-auth-service/target/*.jar app.jar
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
