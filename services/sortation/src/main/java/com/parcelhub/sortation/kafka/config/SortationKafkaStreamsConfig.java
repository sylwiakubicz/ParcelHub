package com.parcelhub.sortation.kafka.config;

import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME;

@Configuration
@EnableKafkaStreams
public class SortationKafkaStreamsConfig {

    private final KafkaProperties kafkaProperties;

    @Value("${iq.host:}")
    private String iqHost;

    @Value("${server.port}")
    private int serverPort;

    public SortationKafkaStreamsConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    // name is here necessary, it tells spring boot -> you do not have to create defaultKafkaStreamsConfig
    // otherwise we will have two configuration for kafka
    @Bean(name = DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration defaultKafkaStreamsConfig() {

        // take configuration from application.properties
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildStreamsProperties());

        if (iqHost != null && !iqHost.isBlank()) {
            props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, iqHost + ":" + serverPort);
        }

        return new KafkaStreamsConfiguration(props);
    }
}
