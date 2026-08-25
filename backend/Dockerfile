# Build Spring Boot application
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src

RUN mvn -B -DskipTests clean package


# Runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S pmtrack && adduser -S pmtrack -G pmtrack

COPY --from=build /app/target/pmtrack-backend-1.0.0.jar app.jar

RUN chown pmtrack:pmtrack app.jar

USER pmtrack

EXPOSE 10000

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
