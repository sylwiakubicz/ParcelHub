package com.parcelhub.courier_app.dto;

import java.util.UUID;

public class ArrivedAtHubRequestDto {
    private UUID shipmentId;
    private String hubId;

    public ArrivedAtHubRequestDto() {
    }

    public UUID getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(UUID shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getHubId() {
        return hubId;
    }

    public void setHubId(String hubId) {
        this.hubId = hubId;
    }
}
