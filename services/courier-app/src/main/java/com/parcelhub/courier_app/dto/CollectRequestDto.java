package com.parcelhub.courier_app.dto;

import java.util.UUID;

public class CollectRequestDto {
    private UUID shipmentId;
    private String lockerId;

    public CollectRequestDto() {}

    public UUID getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(UUID shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getLockerId() {
        return lockerId;
    }

    public void setLockerId(String lockerId) {
        this.lockerId = lockerId;
    }
}
