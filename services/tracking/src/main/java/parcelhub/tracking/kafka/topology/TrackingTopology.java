package parcelhub.tracking.kafka.topology;


import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import parcelhub.tracking.kafka.config.TopicConfig;
import parcelhub.tracking.kafka.dto.TrackingDelta;
import parcelhub.tracking.kafka.mapper.DeltaMapper;

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
    public KStream<String, TrackingDelta> shipmentEventsStream(StreamsBuilder builder) {

        KStream<String, SpecificRecord> stream =
                builder.stream(topicConfig.getShipmentEvents());

        var mapper = new DeltaMapper();

        KStream<String, TrackingDelta> deltas = stream
                .transformValues(() -> new DeltaMappingTransformer(mapper), Named.as("to-delta"))
                .filter((k, d) -> d != null)
                .selectKey((k, d) -> d.getShipmentId())
                .peek((k, d) -> log.info(
                        "DELTA key={} changedAt={} status={} locType={} locId={} dest={} vBump={}",
                        k,
                        d.getChangedAt(),
                        d.getNewStatus(),
                        d.getNewLocationType(),
                        d.getNewLocationId(),
                        d.getDestinationLockerId(),
                        d.getVersionBump()
                ));

        return deltas;
    }
}