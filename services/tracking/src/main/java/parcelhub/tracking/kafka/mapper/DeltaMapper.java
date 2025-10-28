package parcelhub.tracking.kafka.mapper;

import com.parcelhub.shipment.ReturnInitiated;
import com.parcelhub.shipment.ShipmentCreated;
import org.apache.avro.specific.SpecificRecord;

import com.parcelhub.tracking.LocationType;
import com.parcelhub.tracking.ShipmentStatus;
import parcelhub.tracking.kafka.dto.TrackingDelta;

import java.util.Optional;

public class DeltaMapper {

    public Optional<TrackingDelta> map(SpecificRecord record, long recordTimestamp) {

        if (record instanceof ShipmentCreated sc) {
            String shipmentId = toStringOrNull(sc.getShipmentId());
            if (shipmentId == null || shipmentId.isBlank()) {
                return Optional.empty();
            }

            TrackingDelta d = TrackingDelta.statusOnly(shipmentId, ShipmentStatus.CREATED, recordTimestamp);

            String dest = toStringOrNull(sc.getDestinationLockerId());
            if (dest != null && !dest.isBlank()) {
                d.setDestinationLockerId(dest);
                d.setNewLocationType(LocationType.LOCKER);
                d.setNewLocationId(dest);
            }

            return d.isNoOp() ? Optional.empty() : Optional.of(d);
        }

        if (record instanceof ReturnInitiated ri) {
            String shipmentId = toStringOrNull(ri.getShipmentId());
            if (shipmentId == null || shipmentId.isBlank()) {
                return Optional.empty();
            }

            long ts = ri.getTs().toEpochMilli();
            TrackingDelta d = TrackingDelta.statusOnly(shipmentId, ShipmentStatus.RETURN_INITIATED, ts);
            return Optional.of(d);
        }

        return Optional.empty();
    }

    private static String toStringOrNull(Object value) {
        return value == null ? null : value.toString();
    }
}
