FROM maven:3.9.1-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=build /app/target/ewm-service-0.0.1-SNAPSHOT.jar ewm-service.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "ewm-service.jar"]