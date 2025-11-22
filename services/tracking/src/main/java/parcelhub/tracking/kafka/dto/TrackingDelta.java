package parcelhub.tracking.kafka.dto;

import com.parcelhub.tracking.LocationType;
import com.parcelhub.tracking.ShipmentStatus;

public class TrackingDelta {
    private String shipmentId;
    private long changedAt;

    private ShipmentStatus newStatus;
    private LocationType newLocationType;
    private String newLocationId;

    private String destinationLockerId;
    private int versionBump = 1;


    public String getShipmentId() { return shipmentId; }
    public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }

    public long getChangedAt() { return changedAt; }
    public void setChangedAt(long changedAt) { this.changedAt = changedAt; }

    public ShipmentStatus getNewStatus() { return newStatus; }
    public void setNewStatus(ShipmentStatus newStatus) { this.newStatus = newStatus; }

    public LocationType getNewLocationType() { return newLocationType; }
    public void setNewLocationType(LocationType newLocationType) { this.newLocationType = newLocationType; }

    public String getNewLocationId() { return newLocationId; }
    public void setNewLocationId(String newLocationId) { this.newLocationId = newLocationId; }

    public String getDestinationLockerId() { return destinationLockerId; }
    public void setDestinationLockerId(String destinationLockerId) { this.destinationLockerId = destinationLockerId; }

    public int getVersionBump() { return versionBump; }
    public void setVersionBump(int versionBump) { this.versionBump = versionBump; }

    public boolean isNoOp() {
        return newStatus == null
                && newLocationType == null
                && destinationLockerId == null;
    }

    public static TrackingDelta statusOnly(String shipmentId, ShipmentStatus status, long changedAt) {
        TrackingDelta d = new TrackingDelta();
        d.setShipmentId(shipmentId);
        d.setNewStatus(status);
        d.setChangedAt(changedAt);
        return d;
    }

    public static TrackingDelta statusAndLocation(String shipmentId, ShipmentStatus status, long changedAt,
                                                  LocationType newLocationType, String newLocationId) {
        TrackingDelta d = new TrackingDelta();
        d.setShipmentId(shipmentId);
        d.setNewStatus(status);
        d.setChangedAt(changedAt);
        d.setNewLocationId(newLocationId);
        d.setNewLocationType(newLocationType);
        return d;
    }
}
