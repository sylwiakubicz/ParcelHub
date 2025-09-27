package com.parcelHub.shipment_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class InitiateReturnResponseDto {
    private UUID shipmentId;

    private final String status = "RETURN_INITIATED";

    public InitiateReturnResponseDto() {
    }

    public void setShipmentId(UUID shipmentId) {
        this.shipmentId = shipmentId;
    }

    public UUID getShipmentId() {
        return shipmentId;
    }

    public String getStatus() {
        return status;
    }
}
