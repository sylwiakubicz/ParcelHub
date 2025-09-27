package com.parcelHub.shipment_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateShipmentRequestDto {
    @NotBlank(message = "Sender name is required")
    private String senderName;

    @Pattern(regexp = "^\\+?\\d{7,15}$", message = "Invalid phone number format")
    private String senderPhone;

    @NotNull(message = "Sender postal code is required")
    @Pattern(regexp = "^\\d{2}-\\d{3}$", message = "Invalid postal code format")
    private String senderZip;

    @NotBlank(message = "Recipient name is required")
    private String recipientName;

    @Pattern(regexp = "^\\+?\\d{7,15}$", message = "Invalid phone number format")
    private String recipientPhone;

    @NotNull(message = "Recipient postal code is required")
    @Pattern(regexp = "^\\d{2}-\\d{3}$", message = "Invalid postal code format")
    private String recipientZip;

    @NotBlank(message = "Destination locker is required")
    private String destinationLockerId;

    public CreateShipmentRequestDto() {
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getSenderZip() {
        return senderZip;
    }

    public void setSenderZip(String senderZip) {
        this.senderZip = senderZip;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getRecipientZip() {
        return recipientZip;
    }

    public void setRecipientZip(String recipientZip) {
        this.recipientZip = recipientZip;
    }

    public String getDestinationLockerId() {
        return destinationLockerId;
    }

    public void setDestinationLockerId(String destinationLockerId) {
        this.destinationLockerId = destinationLockerId;
    }
}
