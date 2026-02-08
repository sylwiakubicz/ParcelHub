package com.parcelhub.sortation.kafka.topology;

import com.parcelhub.shipment.ArrivedAtHub;
import com.parcelhub.shipment.ShipmentCreated;
import com.parcelhub.sortation.kafka.config.TopicsConfig;
import com.parcelhub.sortation.kafka.dto.ArrivedAtHubWithRoute;
import com.parcelhub.sortation.kafka.dto.ShipmentRoute;
import com.parcelhub.sortation.kafka.mapper.ShipmentRouteMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

import static com.parcelhub.sortation.kafka.topology.TopologyNames.*;

@Configuration
public class SortationTopology {

    private final TopicsConfig topicsConfig;
    private static final Logger log = LoggerFactory.getLogger(SortationTopology.class);

    public SortationTopology(TopicsConfig topicsConfig) {
        this.topicsConfig = topicsConfig;
    }

    @Bean
    public KStream<String, ArrivedAtHub> arrivedAtHubStream(StreamsBuilder builder) {
        KStream<String, ArrivedAtHub> stream = builder.stream(topicsConfig.getScanEventsHub(),
                Consumed.as(ARRIVED_AT_HUB_SOURCE));

        stream.peek((k, v) ->
                log.info("ArrivedAtHub key={} hubId={}", k, v != null ? v.getHubId() : null));

        return stream;
    }

    @Bean
    public KTable<String, ShipmentRoute> shipmentCreatedKTable(StreamsBuilder builder) {
        ShipmentRouteMapper shipmentRouteMapper = new ShipmentRouteMapper();

        JsonSerde<ShipmentRoute> routeSerde = new JsonSerde<>(ShipmentRoute.class);

        Materialized<String, ShipmentRoute, KeyValueStore<Bytes, byte[]>> materialized =
                Materialized.<String, ShipmentRoute, KeyValueStore<org.apache.kafka.common.utils.Bytes,
                                byte[]>>as(SHIPMENT_ROUTE_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(routeSerde);

        KTable<String, ShipmentRoute> ktable = builder.stream(topicsConfig.getShipmentEvents())
                .filter((k, v) -> v instanceof ShipmentCreated, Named.as(ROUTE_FROM_SHIPMENT_CREATED))
                .mapValues(v -> (ShipmentCreated) v)
                .filter((k, sc) -> sc.getShipmentId() != null && sc.getDestinationLockerId() != null)
                .selectKey((k, sc) -> sc.getShipmentId(), Named.as(KEY_BY_SHIPMENT_ID_FROM_CREATED))
                .repartition(Repartitioned.<String, ShipmentCreated>as(SHIPMENT_BY_ID_REPARTITION)
                        .withKeySerde(Serdes.String()))
                .mapValues(shipmentRouteMapper::from)
                .toTable(materialized);

        return ktable;
    }

    @Bean
    public KStream<String, ArrivedAtHubWithRoute> arrivedAtHubJoiner(
            KStream<String, ArrivedAtHub> arrivedAtHubKStream,
            KTable<String, ShipmentRoute> shipmentCreatedKTable
    ) {
        KStream<String, ArrivedAtHub> keyedArrivals = arrivedAtHubKStream
                .filter((k, v) -> v != null && v.getShipmentId() != null)
                .selectKey((k, v) -> v.getShipmentId().toString(),
                        Named.as(KEY_BY_SHIPMENT_ID_FROM_ARRIVED));

        KStream<String, ArrivedAtHubWithRoute> arrivalsWithRoute = keyedArrivals.leftJoin(
                shipmentCreatedKTable,
                ArrivedAtHubWithRoute::new
        );

        // handle missing data
        KStream<String, ArrivedAtHubWithRoute> missingRoute = arrivalsWithRoute
                .filter((k,v) -> v.route() == null, Named.as(MISSING_ROUTE_META));

        missingRoute.peek((k,v) ->
                log.warn("MISSING DATA: shipmentId=" + k + " hubId=" + v.arrivedAtHub().getHubId()));

        // decision can be made
        KStream<String, ArrivedAtHubWithRoute> readyForDecision = arrivalsWithRoute
                .filter((k, v) -> v.route() != null, Named.as(READY_FOR_DECISION));

        readyForDecision.peek((k, v) ->
                log.info("JOIN OK: shipmentId={} hubId={} destLocker={}",
                        k,
                        v.arrivedAtHub().getHubId(),
                        v.route().getDestinationLockerId())
        );

        return readyForDecision;
    }

    // TODO based on readyForDecision stream decide hub or locker and send message with proper schema to ShipmentEvents
}
