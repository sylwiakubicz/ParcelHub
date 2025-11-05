package parcelhub.tracking.kafka.topology;

import com.parcelhub.tracking.ShipmentStatus;
import com.parcelhub.tracking.ShipmentTrackingState;
import com.parcelhub.tracking.TrackingUpdated;
import org.apache.kafka.streams.kstream.ValueTransformerWithKey;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.UUID;

import static parcelhub.tracking.kafka.topology.TopologyNames.STATUS_INDEX_STORE;

public class TrackingUpdateTransformer implements ValueTransformerWithKey<String, ShipmentTrackingState, TrackingUpdated> {

    private KeyValueStore<String, String> index;

    @SuppressWarnings("unchecked")
    @Override
    public void init(ProcessorContext context) {
        this.index = (KeyValueStore<String, String>) context.getStateStore(STATUS_INDEX_STORE);
    }

    @Override
    public TrackingUpdated transform(String shipmentId, ShipmentTrackingState state) {
        if (state == null) return null;

        String prev = index.get(shipmentId);
        ShipmentStatus newStatus = state.getStatus();

        if (prev != null && prev.equals(newStatus.name())) {
            return null;
        }

        TrackingUpdated.Builder b = TrackingUpdated.newBuilder()
                .setEventId(UUID.randomUUID())
                .setShipmentId(state.getShipmentId())
                .setOldStatus(prev != null ? ShipmentStatus.valueOf(prev) : null)
                .setNewStatus(newStatus)
                .setChangedAt(state.getLastUpdate())
                .setLocation(state.getLastLocation());

        index.put(shipmentId, newStatus.name());

        return b.build();
    }

    @Override
    public void close() { }
}
