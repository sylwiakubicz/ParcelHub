FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /tracking

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre
COPY --from=builder /courier-app/target/courier-app-0.0.1-SNAPSHOT.jar /courier.jar
ENTRYPOINT ["java", "-jar", "/tracking.jar"]

