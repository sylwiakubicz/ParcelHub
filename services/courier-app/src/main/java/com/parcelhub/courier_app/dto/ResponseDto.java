package com.parcelhub.courier_app.dto;

import java.util.UUID;

public class ResponseDto {
    private String courierId;
    private UUID shipmentId;
    private String scanEvent;

    public ResponseDto(String courierId, UUID shipmentId, String scanEvent) {
        this.courierId = courierId;
        this.shipmentId = shipmentId;
        this.scanEvent = scanEvent;
    }

    public String getCourierId() {
        return courierId;
    }

    public void setCourierId(String courierId) {
        this.courierId = courierId;
    }

    public UUID getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(UUID shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getScanEvent() {
        return scanEvent;
    }

    public void setScanEvent(String scanEvent) {
        this.scanEvent = scanEvent;
    }
}
