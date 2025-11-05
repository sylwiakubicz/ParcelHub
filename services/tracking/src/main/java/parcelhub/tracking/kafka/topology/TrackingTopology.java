package parcelhub.tracking.kafka.topology;

import com.parcelhub.tracking.ShipmentTrackingState;
import com.parcelhub.tracking.TrackingUpdated;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;
import parcelhub.tracking.kafka.config.TopicConfig;
import parcelhub.tracking.kafka.dto.TrackingDelta;
import parcelhub.tracking.kafka.logic.TrackingAggregator;
import parcelhub.tracking.kafka.mapper.DeltaMapper;
import parcelhub.tracking.kafka.transformer.DeltaMappingTransformer;
import parcelhub.tracking.kafka.transformer.TrackingUpdateTransformer;

import static parcelhub.tracking.kafka.topology.TopologyNames.TRACKING_TABLE_STORE;

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
    public KStream<String, TrackingDelta> trackingDeltas(StreamsBuilder builder) {

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

    @Bean
    public KTable<String, ShipmentTrackingState> shipmentTrackingTable(KStream<String, TrackingDelta> trackingDeltas) {
        TrackingAggregator aggregator = new TrackingAggregator();

        Aggregator<String, TrackingDelta, ShipmentTrackingState> adder = (key, delta, current) ->
                aggregator.apply(current, delta);

        Initializer<ShipmentTrackingState> initializer = aggregator::init;

        Materialized<String, ShipmentTrackingState, KeyValueStore<Bytes, byte[]>> materialized =
                Materialized.<String, ShipmentTrackingState, KeyValueStore<Bytes, byte[]>>as(TRACKING_TABLE_STORE)
                        .withKeySerde(Serdes.String());

        JsonSerde<TrackingDelta> deltaSerde = new JsonSerde<>(TrackingDelta.class);

        var table = trackingDeltas
                .groupByKey(Grouped.with("deltas-by-shipment", Serdes.String(), deltaSerde))
                .aggregate(initializer, adder, materialized);

        table.toStream().peek((k,v) ->
                log.info("STATE key={} status={} lastUpdate={} locType={} locId={} dest={} ver={}",
                        k,
                        v.getStatus(),
                        v.getLastUpdate(),
                        v.getLastLocation() != null ? v.getLastLocation().getType() : null,
                        v.getLastLocation() != null ? v.getLastLocation().getId() : null,
                        v.getDestinationLockerId(),
                        v.getVersion()
                )
        );

        table.toStream().to(topicConfig.getShipmentTracking());

        return table;
    }

    @Bean
    public KStream<String, TrackingUpdated> trackingUpdatesStream(
            StreamsBuilder builder,
            @Qualifier("statusIndexStoreBuilder") StoreBuilder<?> statusIndexStoreBuilder,
            KTable<String, ShipmentTrackingState> shipmentTrackingTable) {

        builder.addStateStore(statusIndexStoreBuilder);

        var updates = shipmentTrackingTable
                .toStream()
                .transformValues(TrackingUpdateTransformer::new,
                        Named.as("status-change-only"),
                        TopologyNames.STATUS_INDEX_STORE)
                .filter((k, evt) -> evt != null);

        updates.to(topicConfig.getTrackingUpdates());

        return updates;
    }

}