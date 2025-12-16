FROM maven:3.9-eclipse-temurin-21 as builder
WORKDIR /locker-gateway

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre
COPY --from=builder /locker-gateway/target/locker-gateway-0.0.1-SNAPSHOT.jar /locker.jar
ENTRYPOINT ["java", "-jar", "/locker.jar"]