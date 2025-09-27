package com.parcelHub.shipment_api.mapper;

import com.parcelHub.shipment_api.dto.LabelResponseDto;
import com.parcelHub.shipment_api.model.Shipment;


public class ShipmentMapper {

    public LabelResponseDto mapShipmentToLabelResponseDto(Shipment shipment) {
        LabelResponseDto labelResponseDto = new LabelResponseDto();
        labelResponseDto.setLabelNumber(shipment.getLabelNumber());
        labelResponseDto.setLabelUrl(shipment.getLabelUrl());
        return labelResponseDto;
    }
}
