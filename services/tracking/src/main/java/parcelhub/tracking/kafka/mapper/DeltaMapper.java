package parcelhub.tracking.kafka.mapper;

import com.parcelhub.shipment.*;
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
                d.setNewLocationType(LocationType.NONE);
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

        if (record instanceof DropOffRegistered dropOff) {
            String shipmentId = toStringOrNull(dropOff.getShipmentId());
            if (shipmentId == null || shipmentId.isBlank()) {
                return Optional.empty();
            }
            long ts = dropOff.getTs().toEpochMilli();
            String dest = dropOff.getLockerId();
            TrackingDelta d = TrackingDelta.statusAndLocation(shipmentId, ShipmentStatus.DROPPED_OFF_AT_LOCKER, ts,
                    LocationType.LOCKER, dest);
            return Optional.of(d);
        }

        if (record instanceof DeliveredToLocker deliveredToLocker) {
            String shipmentId = toStringOrNull(deliveredToLocker.getShipmentId());
            if (shipmentId == null || shipmentId.isBlank()) {
                return Optional.empty();
            }
            long ts = deliveredToLocker.getTs().toEpochMilli();
            String dest = toStringOrNull(deliveredToLocker.getLockerId());

            TrackingDelta d = TrackingDelta.statusAndLocation(shipmentId, ShipmentStatus.DELIVERED_TO_LOCKER, ts,
                    LocationType.LOCKER, dest);
            return Optional.of(d);
        }

        if (record instanceof ReadyForPickup readyForPickup) {
            String shipmentId = toStringOrNull(readyForPickup.getShipmentId());
            if (shipmentId == null || shipmentId.isBlank()) {
                return Optional.empty();
            }
            long ts = readyForPickup.getTs().toEpochMilli();
            TrackingDelta d = TrackingDelta.statusOnly(shipmentId, ShipmentStatus.READY_FOR_PICKUP, ts);
            return Optional.of(d);
        }

        if (record instanceof PickupConfirmed pickupConfirmed) {
            String shipmentId = toStringOrNull(pickupConfirmed.getShipmentId());
            if (shipmentId == null || shipmentId.isBlank()) {
                return Optional.empty();
            }
            String dest = toStringOrNull(pickupConfirmed.getLockerId());
            long ts = pickupConfirmed.getTs().toEpochMilli();
            TrackingDelta d = TrackingDelta.statusAndLocation(shipmentId, ShipmentStatus.PICKED_UP, ts,
                    LocationType.NONE, dest);
            return Optional.of(d);
        }
        return Optional.empty();
    }

    private static String toStringOrNull(Object value) {
        return value == null ? null : value.toString();
    }
}
