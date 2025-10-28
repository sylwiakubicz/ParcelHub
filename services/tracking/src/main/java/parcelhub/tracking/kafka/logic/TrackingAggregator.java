package parcelhub.tracking.kafka.logic;

import com.parcelhub.tracking.LastLocation;
import com.parcelhub.tracking.LocationType;
import com.parcelhub.tracking.ShipmentStatus;
import com.parcelhub.tracking.ShipmentTrackingState;
import parcelhub.tracking.kafka.dto.TrackingDelta;

import java.time.Instant;
import java.util.UUID;

public class TrackingAggregator {

    private static long toMillis(Instant i) {
        return i == null ? 0L : i.toEpochMilli();
    }

    private static Instant toInstant(long millis) {
        return Instant.ofEpochMilli(millis);
    }

    private static UUID toUuidOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return UUID.fromString(s); } catch (IllegalArgumentException e) { return null; }
    }

    public ShipmentTrackingState init() {
        ShipmentTrackingState state = new ShipmentTrackingState();
        state.setShipmentId(null);
        state.setStatus(ShipmentStatus.CREATED);
        state.setLastUpdate(Instant.EPOCH);
        state.setVersion(0);

        LastLocation lastLocation = new LastLocation();
        lastLocation.setType(LocationType.NONE);
        lastLocation.setId(null);
        state.setLastLocation(lastLocation);

        state.setDestinationLockerId(null);
        return state;
    }

    public ShipmentTrackingState apply(ShipmentTrackingState current, TrackingDelta delta) {
        if (current == null) {
            current = init();
        }
        if (delta == null || delta.isNoOp()) {
            return current;
        }

        if (current.getShipmentId() == null) {
            UUID id = toUuidOrNull(delta.getShipmentId());
            current.setShipmentId(id);
        }
        long stateTs = toMillis(current.getLastUpdate());
        long deltaTs = delta.getChangedAt();

        if (deltaTs < stateTs) {
            return current;
        }

        boolean accepted = false;
        if (deltaTs > stateTs) {
            accepted = true;
        } else {
            boolean hasStatus = (delta.getNewStatus() != null);
            boolean hasLocation = (delta.getNewLocationId() != null);
            boolean hasDest = (delta.getDestinationLockerId() != null);

            accepted = hasStatus || hasLocation || hasDest;
        }

        if (!accepted) {
            return current;
        }

        if (delta.getNewStatus() != null) {
            current.setStatus(delta.getNewStatus());
        }

        if (delta.getNewLocationId() != null) {
            LastLocation location = current.getLastLocation();
            if (location == null) {
                location = new LastLocation();
                current.setLastLocation(location);
            }
            location.setType(delta.getNewLocationType());
            location.setId(delta.getNewLocationId());
        }

        if (delta.getDestinationLockerId() != null) {
            current.setDestinationLockerId(delta.getDestinationLockerId());
        }

        current.setLastUpdate(toInstant(deltaTs));
        current.setVersion(current.getVersion() + 1);

        return current;
    }

}
