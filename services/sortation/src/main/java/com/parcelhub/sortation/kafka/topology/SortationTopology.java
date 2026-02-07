package com.parcelhub.sortation.kafka.topology;

import com.parcelhub.shipment.ArrivedAtHub;
import com.parcelhub.sortation.kafka.config.TopicsConfig;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SortationTopology {

    private final TopicsConfig topicsConfig;

    public SortationTopology(TopicsConfig topicsConfig) {
        this.topicsConfig = topicsConfig;
    }

    @Bean
    public KStream<String, ArrivedAtHub> arrivedAtHub(StreamsBuilder builder) {
        KStream<String, ArrivedAtHub> stream = builder.stream(topicsConfig.getScanEventsHub());

        stream.peek((k, v) -> System.out.println("Arrived at " + v.getHubId()));

        return stream;
    }

}
