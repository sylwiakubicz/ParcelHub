package parcelhub.tracking.tracking.kafka.topology;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import parcelhub.tracking.tracking.kafka.props.TopicNames;

@Configuration
public class TrackingTopology {

    @Autowired
    private TopicNames topicNames;

    @Bean
    KStream<Object, Object> trackingSource(StreamsBuilder builder) {
        // rejestracja zrodla
        // ten stream subskrybuje topic shipmentEvents,
        // ktory dziala na domyslnym serde ustawionym w kafka config
        var stream = builder.stream(topicNames.getShipmentEvents());
        stream.foreach((k,v) -> {});
        return stream;
    }
}
