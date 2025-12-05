package com.parcelhub.locker_gateway.dto;

public class ReadyToPickupResponseDto {
    private String shipmentId;
    private ShipmentStatus shipmentStatus;
    private String pickupCode;

    public ReadyToPickupResponseDto(String shipmentId, ShipmentStatus shipmentStatus, String pickupCode) {
        this.shipmentId = shipmentId;
        this.shipmentStatus = shipmentStatus;
        this.pickupCode = pickupCode;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public ShipmentStatus getShipmentStatus() {
        return shipmentStatus;
    }

    public void setShipmentStatus(ShipmentStatus shipmentStatus) {
        this.shipmentStatus = shipmentStatus;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(String pickupCode) {
        this.pickupCode = pickupCode;
    }
}
