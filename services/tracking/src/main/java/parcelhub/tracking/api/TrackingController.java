package parcelhub.tracking.api;

import com.parcelhub.tracking.ShipmentTrackingState;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static parcelhub.tracking.kafka.topology.TopologyNames.TRACKING_TABLE_STORE;

@RestController
@RequestMapping("/shipments")
public class TrackingController {

    private final StreamsBuilderFactoryBean factoryBean;

    public TrackingController(StreamsBuilderFactoryBean factoryBean) {
        this.factoryBean = factoryBean;
    }

    @GetMapping("/{id}/tracking")
    public ResponseEntity<TrackingResponseDto> tracking(@PathVariable String id) {
        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        ReadOnlyKeyValueStore<String, ShipmentTrackingState> trackingStateStore = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(TRACKING_TABLE_STORE, QueryableStoreTypes.keyValueStore())
        );

        ShipmentTrackingState state = trackingStateStore.get(id);
        if (state == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(TrackingResponseDto.from(state));
    }
}
