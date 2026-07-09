# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -B -pl scraper-service -am -DskipTests package

FROM mcr.microsoft.com/playwright/java:v1.49.0-jammy
WORKDIR /app
COPY --from=build /workspace/scraper-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
