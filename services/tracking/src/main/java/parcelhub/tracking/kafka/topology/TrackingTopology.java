package parcelhub.tracking.kafka.topology;


import io.apicurio.registry.serde.avro.AvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import parcelhub.tracking.kafka.config.TopicConfig;

import java.util.Map;

@Configuration
public class TrackingTopology {
    private static final Logger log = LoggerFactory.getLogger(TrackingTopology.class);

    private final TopicConfig topicConfig;

    @Value("${apicurio.url}")
    private String apicurioUrl;

    public TrackingTopology(TopicConfig topicConfig) {
        this.topicConfig = topicConfig;
    }

    @Bean
    public KStream<String, GenericRecord> smokeStream(StreamsBuilder builder) {
        var keySerde = Serdes.String();

        var valueSerde = new AvroSerde<GenericRecord>();
        valueSerde.configure(Map.of(
                "apicurio.registry.url", apicurioUrl,
                "apicurio.registry.find-latest", true,
                "apicurio.registry.use.headers", true
        ), false);

        KStream<String, GenericRecord> stream =
                builder.stream(topicConfig.getShipmentEvents(), Consumed.with(keySerde, valueSerde));

        stream.peek((key, value) -> {
            String schema = (value != null && value.getSchema() != null)
                    ? value.getSchema().getFullName()
                    : "null";
            log.info("IN: topic={}, key={}, valueSchema={}", topicConfig.getShipmentEvents(), key, schema);
        });

        return stream;
    }

}


//outSerde.configure(Map.of(
//                           "apicurio.registry.url", apicurioUrl,
//    "apicurio.registry.find-latest", true,
//                           "apicurio.registry.artifact.resolver.strategy",
//                           "io.apicurio.registry.serde.strategy.TopicRecordIdStrategy",
//                           "apicurio.registry.artifact.group-id", "shipment",
//                           "apicurio.registry.use.headers", false,
//                   // w DEV możesz dodać:
//                   // "apicurio.registry.auto-register", true
//), /*isKey*/ false);