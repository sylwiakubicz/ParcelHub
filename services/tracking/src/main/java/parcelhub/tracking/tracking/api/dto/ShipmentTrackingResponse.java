package parcelhub.tracking.tracking.api.dto;

import parcelhub.tracking.tracking.api.enums.ShipmentStatus;

import java.time.Instant;

public class ShipmentTrackingResponse {
    private String shipmentId;
    private ShipmentStatus status;
    private Instant lastUpdate;
    private Location lastLocation;
    private String destinationLockerId;
    private Integer version;

    public ShipmentTrackingResponse() {
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public String getDestinationLockerId() {
        return destinationLockerId;
    }

    public void setDestinationLockerId(String destinationLockerId) {
        this.destinationLockerId = destinationLockerId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
