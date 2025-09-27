package com.parcelHub.shipment_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LabelResponseDto {
    private String labelUrl;
    private String labelNumber;

    public LabelResponseDto() {
    }

    public void setLabelNumber(String labelNumber) {
        this.labelNumber = labelNumber;
    }

    public void setLabelUrl(String labelUrl) {
        this.labelUrl = labelUrl;
    }
}
