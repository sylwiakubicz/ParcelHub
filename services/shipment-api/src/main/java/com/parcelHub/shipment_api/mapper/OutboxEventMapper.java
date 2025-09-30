package com.parcelHub.shipment_api.mapper;

import com.parcelHub.shipment_api.dto.OutboxPayloadDto;
import com.parcelHub.shipment_api.dto.PersonDto;
import com.parcelHub.shipment_api.model.Shipment;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventMapper {

    public OutboxPayloadDto mapShipmentToOutboxPayloadDto(Shipment shipment) {
        OutboxPayloadDto outboxPayloadDto = new OutboxPayloadDto();
        outboxPayloadDto.setCreatedAt(shipment.getCreatedAt());
        outboxPayloadDto.setShipmentId(shipment.getId());
        outboxPayloadDto.setRecipient(mapShipmentToRecipientDto(shipment));
        outboxPayloadDto.setSender(mapShipmentToSenderDto(shipment));
        outboxPayloadDto.setDestinationLockerId(shipment.getDestinationLockerId());

        return outboxPayloadDto;
    }

    public PersonDto mapShipmentToSenderDto(Shipment shipment) {
        PersonDto personDto = new PersonDto();
        personDto.setName(shipment.getSenderName());
        personDto.setPhone(shipment.getSenderPhone());
        personDto.setZip(shipment.getSenderZip());
        return personDto;
    }


    public PersonDto mapShipmentToRecipientDto(Shipment shipment) {
        PersonDto personDto = new PersonDto();
        personDto.setName(shipment.getRecipientName());
        personDto.setPhone(shipment.getRecipientPhone());
        personDto.setZip(shipment.getRecipientZip());
        return personDto;
    }
}
