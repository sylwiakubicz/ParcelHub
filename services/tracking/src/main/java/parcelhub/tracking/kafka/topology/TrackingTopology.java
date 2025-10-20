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

        builder.stream(topicConfig.getShipmentEvents(), Consumed.with(keySerde, valueSerde))
                .process(() -> new org.apache.kafka.streams.processor.api.Processor<String, GenericRecord, Void, Void>() {

                    private org.apache.kafka.streams.processor.api.ProcessorContext<Void, Void> context;

                    @Override
                    public void init(org.apache.kafka.streams.processor.api.ProcessorContext<Void, Void> context) {
                        this.context = context;
                    }

                    @Override
                    public void process(org.apache.kafka.streams.processor.api.Record<String, GenericRecord> record) {
                        // 1) event_type z nagłówka
                        String eventType = "UNKNOWN";
                        var h = record.headers().lastHeader("event_type");
                        if (h != null && h.value() != null) {
                            eventType = new String(h.value(), java.nio.charset.StandardCharsets.UTF_8);
                        }

                        // 2) shipmentId / destinationLockerId z Avro GenericRecord
                        String shipmentId = null;
                        String destLocker = null;

                        if (record.value() != null) {
                            Object sid = record.value().get("shipmentId");
                            if (sid instanceof CharSequence cs) shipmentId = cs.toString();

                            Object dli = record.value().get("destinationLockerId");
                            if (dli instanceof CharSequence cs) destLocker = cs.toString();
                        }

                        long ts = record.timestamp(); // timestamp rekordu z Kafki

                        log.info(
                                "TRACK-IN eventType={}, key={}, shipmentId={}, destinationLockerId={}, ts={}",
                                eventType, record.key(), shipmentId, destLocker, ts
                        );
                    }
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