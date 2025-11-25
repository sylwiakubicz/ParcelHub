package com.parcelhub.locker_gateway.dto;

public class TrackingClientResponse {
    private ShipmentStatus status;
    private long lastUpdated;
    private LastLocationClientResponse lastLocation;
    private String destinationLockerId;
    private int version;

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public LastLocationClientResponse getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(LastLocationClientResponse lastLocation) {
        this.lastLocation = lastLocation;
    }

    public String getDestinationLockerId() {
        return destinationLockerId;
    }

    public void setDestinationLockerId(String destinationLockerId) {
        this.destinationLockerId = destinationLockerId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
