package com.parcelHub.shipment_api.mapper;

import com.parcelHub.shipment_api.dto.CreateShipmentRequestDto;
import com.parcelHub.shipment_api.dto.CreateShipmentResponseDto;
import com.parcelHub.shipment_api.dto.LabelResponseDto;
import com.parcelHub.shipment_api.model.Shipment;
import org.springframework.stereotype.Component;

@Component
public class ShipmentMapper {

    public LabelResponseDto mapShipmentToLabelResponseDto(Shipment shipment) {
        LabelResponseDto labelResponseDto = new LabelResponseDto();
        labelResponseDto.setLabelNumber(shipment.getLabelNumber());
        labelResponseDto.setLabelUrl(shipment.getLabelUrl());
        return labelResponseDto;
    }

    public CreateShipmentResponseDto mapShipmentToCreateShipmentResponseDto(Shipment shipment) {
        CreateShipmentResponseDto createShipmentResponseDto = new CreateShipmentResponseDto();
        createShipmentResponseDto.setCreatedAt(shipment.getCreatedAt());
        createShipmentResponseDto.setShipmentId(shipment.getId());
        createShipmentResponseDto.setLabelNumber(shipment.getLabelNumber());
        createShipmentResponseDto.setLabelUrl(shipment.getLabelUrl());
        return createShipmentResponseDto;
    }

    public Shipment mapCreateShipmentRequestDtoToShipment(CreateShipmentRequestDto createShipmentRequestDto) {
        Shipment shipment = new Shipment();
        shipment.setSenderName(createShipmentRequestDto.getSender().getName());
        shipment.setSenderPhone(trimToNull(createShipmentRequestDto.getSender().getPhone()));
        shipment.setSenderZip(createShipmentRequestDto.getSender().getZip());
        shipment.setRecipientPhone(createShipmentRequestDto.getRecipient().getPhone());
        shipment.setRecipientZip(createShipmentRequestDto.getRecipient().getZip());
        shipment.setRecipientName(createShipmentRequestDto.getRecipient().getName());
        shipment.setDestinationLockerId(createShipmentRequestDto.getDestinationLockerId());
        if (createShipmentRequestDto.getClientRequestId() != null) {
            shipment.setClientRequestId(createShipmentRequestDto.getClientRequestId());
        }
        return shipment;
    }

    private String trimToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
