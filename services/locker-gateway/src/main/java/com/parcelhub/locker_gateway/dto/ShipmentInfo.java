package com.parcelhub.locker_gateway.dto;

public class ShipmentInfo {
    private String shipmentId;
    private ShipmentStatus status;              // albo enum ShipmentStatus
    private String destinationLockerId;

    public ShipmentInfo(String shipmentId, ShipmentStatus status, String destinationLockerId) {
        this.shipmentId = shipmentId;
        this.status = status;
        this.destinationLockerId = destinationLockerId;
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

    public String getDestinationLockerId() {
        return destinationLockerId;
    }

    public void setDestinationLockerId(String destinationLockerId) {
        this.destinationLockerId = destinationLockerId;
    }
}
