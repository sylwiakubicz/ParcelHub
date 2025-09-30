package com.parcelHub.shipment_api.controller;

import com.parcelHub.shipment_api.dto.*;
import com.parcelHub.shipment_api.service.ShipmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController("/shipments")
public class ShipmentController {

    private ShipmentService shipmentService;

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
            @RequestHeader(value = "X-Trace-Id", required = false) @Pattern(regexp = "") String traceId,
            @RequestHeader(value = "X-Correlation-ID", required = false) @Pattern(regexp = "") String correlationId,
            @Valid @RequestBody CreateShipmentRequestDto createShipmentRequestDto) {
        CreateShipmentResponseDto createShipmentResponseDto = shipmentService.createShipment(
                createShipmentRequestDto, traceId, correlationId);
        return ResponseEntity.ok(createShipmentResponseDto);
    }

    @PostMapping("/{shipmentId}/returns")
    public ResponseEntity<InitiateReturnResponseDto> initiateReturn(
            @PathVariable UUID shipmentId,
            @Valid @RequestBody InitiateReturnRequestDto initiateReturnRequestDto) {
        InitiateReturnResponseDto initiateReturnResponseDto =
                shipmentService.initiateReturn(shipmentId, initiateReturnRequestDto);
        return ResponseEntity.ok(initiateReturnResponseDto);
    }
}
