package com.parcelHub.shipment_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.parcelHub.shipment_api.dto.OutboxPayloadDto;
import com.parcelHub.shipment_api.mapper.OutboxEventMapper;
import com.parcelHub.shipment_api.model.OutboxEvent;
import com.parcelHub.shipment_api.model.Shipment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OutboxEventFactory {
    private static final String SOURCE = "shipment-api";
    private static final String AGGREGATE_TYPE = "shipment";
    private static final String SCHEMA_VERSION = "1.0";
    private final ObjectMapper objectMapper;
    private final OutboxEventMapper outboxEventMapper;

    public OutboxEventFactory(ObjectMapper objectMapper, OutboxEventMapper outboxEventMapper) {
        this.objectMapper = objectMapper;
        this.outboxEventMapper = outboxEventMapper;
    }

    public OutboxEvent createOutboxEvent(Shipment shipment, String traceId, String correlationId) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setId(UUID.randomUUID());
        outboxEvent.setEventType("ShipmentCreated");
        outboxEvent.setAggregateType(AGGREGATE_TYPE);
        outboxEvent.setAggregateId(shipment.getId());

        OutboxPayloadDto outboxPayloadDto = outboxEventMapper.mapShipmentToOutboxPayloadDto(shipment);
        ObjectNode payload = objectMapper.valueToTree(outboxPayloadDto);
        outboxEvent.setPayload(payload);

        ObjectNode headers = objectMapper.createObjectNode();
        headers.put("traceId", traceId);
        headers.put("correlationId", correlationId);
        headers.put("schemaVersion", SCHEMA_VERSION);
        headers.put("source", SOURCE);
        outboxEvent.setHeaders(headers);

        return outboxEvent;
    }
}
