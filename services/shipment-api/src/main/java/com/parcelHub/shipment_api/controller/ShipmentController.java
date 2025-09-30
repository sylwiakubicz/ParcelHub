package com.parcelHub.shipment_api.controller;

import com.parcelHub.shipment_api.dto.*;
import com.parcelHub.shipment_api.service.ShipmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;
    private static final String REGEX_UUID =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping("/{shipmentId}/label")
    public ResponseEntity<LabelResponseDto> getShipment(@PathVariable UUID shipmentId) {
        LabelResponseDto labelResponseDto = shipmentService.getShipment(shipmentId);
        return ResponseEntity.ok(labelResponseDto);
    }

    @PostMapping
    public ResponseEntity<CreateShipmentResponseDto> createShipment(
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @Valid @RequestBody CreateShipmentRequestDto createShipmentRequestDto) {

        if (traceId != null && !traceId.matches(REGEX_UUID)) traceId = null;
        if (correlationId != null && !correlationId.matches(REGEX_UUID)) correlationId = null;

        CreateShipmentResponseDto createShipmentResponseDto = shipmentService.createShipment(
                createShipmentRequestDto, traceId, correlationId);
        return ResponseEntity.ok(createShipmentResponseDto);
    }

    @PostMapping("/{shipmentId}/returns")
    public ResponseEntity<InitiateReturnResponseDto> initiateReturn(
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @PathVariable UUID shipmentId,
            @Valid @RequestBody InitiateReturnRequestDto initiateReturnRequestDto) {

        if (traceId != null && !traceId.matches(REGEX_UUID)) traceId = null;
        if (correlationId != null && !correlationId.matches(REGEX_UUID)) correlationId = null;

        InitiateReturnResponseDto initiateReturnResponseDto =
                shipmentService.initiateReturn(shipmentId, initiateReturnRequestDto, traceId, correlationId);
        return ResponseEntity.ok(initiateReturnResponseDto);
    }
}
