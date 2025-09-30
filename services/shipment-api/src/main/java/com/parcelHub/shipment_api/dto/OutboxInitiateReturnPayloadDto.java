package com.parcelHub.shipment_api.dto;

import java.util.UUID;

public class OutboxInitiateReturnPayloadDto {
    private UUID eventId;
    private UUID shipmentId;
    private String reason;
    private long ts;

    public OutboxInitiateReturnPayloadDto() {
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(UUID shipmentId) {
        this.shipmentId = shipmentId;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }


}
