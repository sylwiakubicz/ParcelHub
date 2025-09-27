package com.parcelHub.shipment_api.controller;

import com.parcelHub.shipment_api.dto.CreateShipmentRequestDto;
import com.parcelHub.shipment_api.dto.CreateShipmentResponseDto;
import com.parcelHub.shipment_api.dto.LabelResponseDto;
import com.parcelHub.shipment_api.service.ShipmentService;
import jakarta.validation.Valid;
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
            @Valid @RequestBody CreateShipmentRequestDto createShipmentRequestDto) {
        CreateShipmentResponseDto createShipmentResponseDto = shipmentService.createShipment(createShipmentRequestDto);
        return ResponseEntity.ok(createShipmentResponseDto);
    }
}
