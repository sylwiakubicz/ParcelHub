package com.parcelHub.shipment_api.dto;

import java.time.Instant;
import java.util.UUID;

public class OutboxPayloadDto {
    private UUID shipmentId;
    private Instant createdAt;
    private PersonDto sender;
    private PersonDto recipient;
    private String destinationLockerId;
    private UUID eventId;

    public OutboxPayloadDto() {
    }

    public UUID getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(UUID shipmentId) {
        this.shipmentId = shipmentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public PersonDto getSender() {
        return sender;
    }

    public void setSender(PersonDto sender) {
        this.sender = sender;
    }

    public PersonDto getRecipient() {
        return recipient;
    }

    public void setRecipient(PersonDto recipient) {
        this.recipient = recipient;
    }

    public String getDestinationLockerId() {
        return destinationLockerId;
    }

    public void setDestinationLockerId(String destinationLockerId) {
        this.destinationLockerId = destinationLockerId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }
}

