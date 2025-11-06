FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /tracking

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean -DskipTests

RUN mvn -q -Pdownload-schemas compile -DskipTests
RUN mvn -q -Pgenerate-shipment-schema-classes compile -DskipTests
RUN mvn -q -Pgenerate-tracking-schema-classes compile -DskipTests
RUN mvn -q -Pregister-schemas compile -DskipTests

RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre
COPY --from=builder /tracking/target/tracking-service-0.0.1-SNAPSHOT.jar /tracking.jar
ENTRYPOINT ["java", "-jar", "/tracking.jar"]