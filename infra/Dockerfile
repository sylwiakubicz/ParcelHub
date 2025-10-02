FROM debezium/connect:2.6
USER root
ENV KAFKA_CONNECT_PLUGIN_PATH=/kafka/connect
RUN mkdir -p /kafka/connect/apicurio \
 && curl -fSL -o /tmp/converters.zip \
    https://repo1.maven.org/maven2/io/apicurio/apicurio-registry-distro-connect-converter/3.0.6.Final/apicurio-registry-distro-connect-converter-2.5.10.Final.zip \
 && unzip /tmp/converters.zip -d /kafka/connect/apicurio \
 && rm /tmp/converters.zip
USER kafka
