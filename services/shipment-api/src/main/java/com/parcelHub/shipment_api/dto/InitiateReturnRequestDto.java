package com.parcelHub.shipment_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class InitiateReturnRequestDto {
    @NotBlank(message = "Reason is required")
    private String reason;

    public InitiateReturnRequestDto() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
