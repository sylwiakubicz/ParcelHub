package com.parcelhub.sortation.kafka.topology;

import com.parcelhub.shipment.ArrivedAtHub;
import com.parcelhub.shipment.ShipmentCreated;
import com.parcelhub.sortation.kafka.config.TopicsConfig;
import com.parcelhub.sortation.kafka.dto.ShipmentRoute;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.internals.KStreamImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SortationTopology {

    private final TopicsConfig topicsConfig;

    public SortationTopology(TopicsConfig topicsConfig) {
        this.topicsConfig = topicsConfig;
    }

    @Bean
    public KStream<String, ArrivedAtHub> arrivedAtHubStream(StreamsBuilder builder) {
        KStream<String, ArrivedAtHub> stream = builder.stream(topicsConfig.getScanEventsHub());

        stream.peek((k, v) -> System.out.println("Arrived at " + v.getHubId()));

        return stream;
    }

    @Bean
    public KTable<String, ShipmentRoute> shipmentCreatedKTable(StreamsBuilder builder) {
        KTable<String, ShipmentRoute> ktable = builder.stream(topicsConfig.getShipmentEvents())
                .filter((k, v) -> v instanceof ShipmentCreated)
                .mapValues(v -> (ShipmentCreated) v)
                .filter((k, sc) -> sc.getShipmentId() != null && sc.getDestinationLockerId() != null)
                .selectKey((k, sc) -> sc.getShipmentId())
                .mapValues(sc -> {
                    ShipmentRoute route = new ShipmentRoute();
                    route.setShipmentId(sc.getShipmentId());
                    route.setDestinationLockerId(sc.getDestinationLockerId());
                    return route;
                })
                .toTable();

        return ktable;
    }

}
