package parcelhub.tracking.kafka.topology;


import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import parcelhub.tracking.kafka.config.TopicConfig;

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
    public KStream<String, SpecificRecord> shipmentEventsStream(StreamsBuilder builder) {

        KStream<String, SpecificRecord> stream =
                builder.stream(topicConfig.getShipmentEvents());

        stream.peek((k,v) -> log.info("class={}, schema={}",
                v == null ? "null" : v.getClass().getName(),
                (v instanceof org.apache.avro.generic.GenericRecord gr) ? gr.getSchema().getFullName()
                        : (v instanceof SpecificRecord sr) ? sr.getSchema().getFullName()
                        : "n/a"));

        stream.process(
                () -> new org.apache.kafka.streams.processor.api.Processor<String, SpecificRecord, Void, Void>() {

                    private org.apache.kafka.streams.processor.api.ProcessorContext<Void, Void> context;

                    @Override
                    public void init(org.apache.kafka.streams.processor.api.ProcessorContext<Void, Void> context) {
                        this.context = context;
                    }

                    @Override
                    public void process(org.apache.kafka.streams.processor.api.Record<String, SpecificRecord> record) {
                        // header "event_type"
                        String eventType = "UNKNOWN";
                        var h = record.headers().lastHeader("event_type");
                        if (h != null && h.value() != null) {
                            eventType = new String(h.value(), java.nio.charset.StandardCharsets.UTF_8);
                        }

                        SpecificRecord v = record.value();
                        String shipmentId = getStringField(v, "shipmentId");
                        String destLocker  = getStringField(v, "destinationLockerId");

                        String schemaName = (v != null && v.getSchema() != null)
                                ? v.getSchema().getFullName()
                                : "null";

                        long ts = record.timestamp();

                        log.info("TRACK-IN eventType={}, schema={}, key={}, shipmentId={}, destinationLockerId={}, ts={}",
                                eventType, schemaName, record.key(), shipmentId, destLocker, ts);
                    }
                }
        );

        return stream;
    }

    private static String getStringField(SpecificRecord rec, String fieldName) {
        if (rec == null || rec.getSchema() == null) return null;
        var field = rec.getSchema().getField(fieldName);
        if (field == null) return null;
        Object val = rec.get(field.pos());
        return val != null ? val.toString() : null;
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