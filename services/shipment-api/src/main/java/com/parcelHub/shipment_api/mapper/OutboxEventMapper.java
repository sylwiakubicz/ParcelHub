package com.parcelHub.shipment_api.mapper;

import com.parcelHub.shipment_api.dto.InitiateReturnRequestDto;
import com.parcelHub.shipment_api.dto.OutboxInitiateReturnPayloadDto;
import com.parcelHub.shipment_api.dto.OutboxPayloadDto;
import com.parcelHub.shipment_api.dto.PersonDto;
import com.parcelHub.shipment_api.model.Shipment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OutboxEventMapper {

    public OutboxPayloadDto mapShipmentToOutboxPayloadDto(Shipment shipment, UUID eventId) {
        OutboxPayloadDto outboxPayloadDto = new OutboxPayloadDto();
        outboxPayloadDto.setCreatedAt(shipment.getCreatedAt());
        outboxPayloadDto.setShipmentId(shipment.getId());
        outboxPayloadDto.setRecipient(mapShipmentToRecipientDto(shipment));
        outboxPayloadDto.setSender(mapShipmentToSenderDto(shipment));
        outboxPayloadDto.setDestinationLockerId(shipment.getDestinationLockerId());
        outboxPayloadDto.setEventId(eventId);

        return outboxPayloadDto;
    }

    public OutboxInitiateReturnPayloadDto mapRequestToOutboxInitiateReturnPayloadDto(
            InitiateReturnRequestDto initiateRequest, UUID shipmentId, UUID eventId) {
        OutboxInitiateReturnPayloadDto outboxInitiateReturnPayloadDto = new OutboxInitiateReturnPayloadDto();
        outboxInitiateReturnPayloadDto.setReason(initiateRequest.getReason());
        outboxInitiateReturnPayloadDto.setShipmentId(shipmentId);
        outboxInitiateReturnPayloadDto.setTs(java.time.Instant.now().toEpochMilli());
        outboxInitiateReturnPayloadDto.setEventId(eventId);

        return outboxInitiateReturnPayloadDto;
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
