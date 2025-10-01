FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /shipment-api

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
COPY --from=builder /shipment-api/target/shipment-api-*-SNAPSHOT.jar /shipment-api.jar
ENTRYPOINT ["java", "-jar", "/shipment-api.jar"]