FROM debezium/connect:2.6
USER root

ENV KAFKA_CONNECT_PLUGIN_PATH=/kafka/connect
ARG APICURIO_VER=2.5.10.Final

RUN mkdir -p /kafka/connect/apicurio \
 && curl -fSL -o /tmp/converters.zip \
    "https://repo1.maven.org/maven2/io/apicurio/apicurio-registry-distro-connect-converter/${APICURIO_VER}/apicurio-registry-distro-connect-converter-${APICURIO_VER}.zip" \
 && unzip /tmp/converters.zip -d /kafka/connect/apicurio \
 && rm /tmp/converters.zip

USER kafka
