package com.parcelhub.locker_gateway.dto;

public class ResponseDto {
    private String shipmentId;
    private ShipmentStatus shipmentStatus;

    public ResponseDto(String shipmentId, ShipmentStatus shipmentStatus) {
        this.shipmentId = shipmentId;
        this.shipmentStatus = shipmentStatus;
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
}
