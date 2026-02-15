package com.parcelhub.notification_service.kafka.topology;

import com.parcelhub.notification.NotificationRequest;
import com.parcelhub.notification_service.kafka.config.TopicConfig;
import com.parcelhub.notification_service.kafka.dto.ShipmentNotificationState;
import com.parcelhub.notification_service.kafka.mapper.ShipmentNotificationMapper;
import com.parcelhub.shipment.ReadyForPickup;
import com.parcelhub.shipment.ShipmentCreated;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.Map;

import static com.parcelhub.notification_service.kafka.topology.TopologyNames.*;

@EnableKafkaStreams
@Configuration
public class NotificationTopology {
    private final TopicConfig topicConfig;

    public NotificationTopology(TopicConfig topicConfig) {
        this.topicConfig = topicConfig;
    }

    @Bean
    public Map<String, KStream<String, SpecificRecord>> topicStreams(StreamsBuilder builder) {
        KStream<String, SpecificRecord> events = builder.stream(topicConfig.getShipmentEvents());

        Map<String, KStream<String, SpecificRecord>> branches = events.split()
                .branch((k, v) -> v instanceof ShipmentCreated, Branched.as(SHIPMENT_NOTIFICATION_BRANCH))
                .branch((k, v) -> v instanceof ReadyForPickup, Branched.as(READY_FOR_PICKUP_BRANCH))
                .noDefaultBranch();

        return branches;
    }

    @Bean
    public KTable<String, ShipmentNotificationState> shipmentStateTable(Map<String, KStream<String, SpecificRecord>> branches) {
        // TODO: 1) handle this exception
        var createdBranch = branches.get(SHIPMENT_NOTIFICATION_BRANCH);
        if (createdBranch == null) {
            throw new IllegalStateException("Missing branches. Got: " + branches.keySet());
        }

        JsonSerde<ShipmentNotificationState> routeSerde = new JsonSerde<>(ShipmentNotificationState.class);

        Materialized<String, ShipmentNotificationState, KeyValueStore<Bytes, byte[]>> materialized =
                Materialized.<String, ShipmentNotificationState, KeyValueStore<org.apache.kafka.common.utils.Bytes,
                                byte[]>>as(SHIPMENT_NOTIFICATION_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(routeSerde);


        KTable<String, ShipmentNotificationState> shipmentNotificationData = branches.get(SHIPMENT_NOTIFICATION_BRANCH)
                .mapValues(v -> (ShipmentCreated) v)
                .selectKey((k, sc) -> sc.getShipmentId())
                .mapValues(ShipmentNotificationMapper::from)
                .toTable(materialized);

        return shipmentNotificationData;
    }

    @Bean
    public KStream<String, ReadyForPickup> shipmentNotifications(Map<String, KStream<String, SpecificRecord>> branches) {
        // TODO: 1) handle this exeption
        var rfpBranch = branches.get(READY_FOR_PICKUP_BRANCH);
        if (rfpBranch == null) {
            throw new IllegalStateException("Missing branches. Got: " + branches.keySet());
        }

        KStream<String, ReadyForPickup> shipmentNotifications = branches.get(READY_FOR_PICKUP_BRANCH)
                .mapValues(v -> (ReadyForPickup) v)
                .selectKey((k, rfp) -> rfp.getShipmentId().toString());

        return shipmentNotifications;
    }

    @Bean
    public KStream<String, NotificationRequest> sendNotifications(
            KStream<String, ReadyForPickup> readyForPickupKStream,
            KTable<String, ShipmentNotificationState> shipmentStateTable
    ) {
        KStream<String, NotificationRequest> out = readyForPickupKStream
                .leftJoin(shipmentStateTable,
                        (LV, RV) ->
                                ShipmentNotificationMapper.createNotificationRequest(RV,LV))
                .filter((k,v) -> v != null);

        out.to(topicConfig.getNotificationRequests());
        return out;
    }
}
