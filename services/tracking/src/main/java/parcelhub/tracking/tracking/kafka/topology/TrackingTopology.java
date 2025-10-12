package parcelhub.tracking.tracking.kafka.topology;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import parcelhub.tracking.tracking.kafka.props.TopicNames;

import com.parcelhub.shipment.ShipmentCreated;
import com.parcelhub.shipment.ShipmentKey;
import com.parcelhub.tracking.Location;
import com.parcelhub.tracking.LocationType;
import com.parcelhub.tracking.ShipmentStatus;
import com.parcelhub.tracking.ShipmentTrackingState;

import java.util.UUID;

@Configuration
public class TrackingTopology {

    @Autowired
    private TopicNames topicNames;

    @Bean
    KStream<ShipmentKey, Object> trackingSource(StreamsBuilder builder) {
        KStream<ShipmentKey, Object> stream = builder.stream(topicNames.getShipmentEvents());

        stream.foreach((k,v) -> {
            System.out.println(((SpecificRecordBase) v).getSchema().getFullName());
        });

        KStream<ShipmentKey, ShipmentTrackingState> states = stream
                .mapValues((key,value) -> buildStateFromEvent(key, (SpecificRecordBase) value))
                .filter((k,v) -> v != null);

        KGroupedStream<ShipmentKey, ShipmentTrackingState> grouped = states.groupByKey();
        KTable<ShipmentKey, ShipmentTrackingState> table = grouped.reduce(
                (oldVal, newVal) -> newVal,
                Materialized.<ShipmentKey, ShipmentTrackingState, KeyValueStore<Bytes, byte[]>>as("tracking-state")
        );

        table.toStream().to(topicNames.getShipmentTracking());
        return stream;
    }

    static ShipmentTrackingState buildStateFromEvent(ShipmentKey key, SpecificRecordBase value) {
        if (value instanceof ShipmentCreated sc) {
            Location none = Location.newBuilder()
                    .setType(LocationType.NONE)
                    .setId(null)
                    .build();

            return ShipmentTrackingState.newBuilder()
                    .setShipmentId(UUID.fromString(key.getShipmentId().toString()))
                    .setStatus(ShipmentStatus.CREATED)
                    .setLastUpdate(sc.getCreatedAt())
                    .setLastLocation(none)
                    .setDestinationLockerId(sc.getDestinationLockerId())
                    .setVersion(1)
                    .build();
        }
        return null;
    }
}
