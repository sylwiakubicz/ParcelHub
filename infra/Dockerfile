FROM debezium/connect:2.6
USER root

ENV KAFKA_CONNECT_PLUGIN_PATH=/kafka/connect
ARG APICURIO_VER=3.1.0

# 1) Converter distro (zip) → plugin path
RUN mkdir -p /kafka/connect/apicurio \
 && curl -fSL -o /tmp/converters.zip \
    "https://repo1.maven.org/maven2/io/apicurio/apicurio-registry-distro-connect-converter/${APICURIO_VER}/apicurio-registry-distro-connect-converter-${APICURIO_VER}.zip" \
 && unzip /tmp/converters.zip -d /kafka/connect/apicurio \
 && rm /tmp/converters.zip

# 2) Kluczowe JARy strategii/serde → globalny classpath Connecta
RUN curl -fSL \
      -o /kafka/libs/apicurio-registry-utils-converter-${APICURIO_VER}.jar \
      "https://repo1.maven.org/maven2/io/apicurio/apicurio-registry-utils-converter/${APICURIO_VER}/apicurio-registry-utils-converter-${APICURIO_VER}.jar" \
 && curl -fSL \
      -o /kafka/libs/apicurio-registry-serde-common-${APICURIO_VER}.jar \
      "https://repo1.maven.org/maven2/io/apicurio/apicurio-registry-serde-common/${APICURIO_VER}/apicurio-registry-serde-common-${APICURIO_VER}.jar" \
 && curl -fSL \
      -o /kafka/libs/apicurio-registry-serde-kafka-common-${APICURIO_VER}.jar \
      "https://repo1.maven.org/maven2/io/apicurio/apicurio-registry-serde-kafka-common/${APICURIO_VER}/apicurio-registry-serde-kafka-common-${APICURIO_VER}.jar"

USER kafka