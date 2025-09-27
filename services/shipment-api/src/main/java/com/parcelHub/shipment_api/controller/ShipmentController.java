package com.parcelHub.shipment_api.controller;

import com.parcelHub.shipment_api.dto.LabelResponseDto;
import com.parcelHub.shipment_api.service.ShipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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
}
