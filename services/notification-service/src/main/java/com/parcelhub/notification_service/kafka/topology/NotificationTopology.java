package com.parcelhub.notification_service.kafka.topology;

import com.parcelhub.notification_service.kafka.config.TopicConfig;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafkaStreams
@Configuration
public class NotificationTopology {
    private final TopicConfig topicConfig;

    public NotificationTopology(TopicConfig topicConfig) {
        this.topicConfig = topicConfig;
    }

    @Bean
    public KStream<String, SpecificRecord> test(StreamsBuilder builder) {
        KStream<String, SpecificRecord> stream = builder.stream(topicConfig.getShipmentEvents());
        return stream;
    }
}
