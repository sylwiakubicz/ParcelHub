FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /sortation

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre
COPY --from=builder /sortation/target/sortation-0.0.1-SNAPSHOT.jar /sortation.jar
ENTRYPOINT ["java", "-jar", "/sortation.jar"]