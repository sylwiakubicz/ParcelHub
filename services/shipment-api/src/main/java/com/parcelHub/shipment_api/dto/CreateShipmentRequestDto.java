package com.parcelHub.shipment_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateShipmentRequestDto {
    @NotNull(message = "Provide sender data")
    private PersonDto sender;

    @NotNull(message = "Provide recipient data")
    private PersonDto recipient;

    @NotBlank(message = "Destination locker is required")
    private String destinationLockerId;

    private UUID clientRequestId;

    public CreateShipmentRequestDto() {
    }

    public PersonDto getRecipient() {
        return recipient;
    }

    public void setRecipient(PersonDto recipient) {
        this.recipient = recipient;
    }

    public PersonDto getSender() {
        return sender;
    }

    public void setSender(PersonDto sender) {
        this.sender = sender;
    }

    public String getDestinationLockerId() {
        return destinationLockerId;
    }

    public void setDestinationLockerId(String destinationLockerId) {
        this.destinationLockerId = destinationLockerId;
    }

    public UUID getClientRequestId() {
        return clientRequestId;
    }

    public void setClientRequestId(UUID clientRequestId) {
        this.clientRequestId = clientRequestId;
    }
}
