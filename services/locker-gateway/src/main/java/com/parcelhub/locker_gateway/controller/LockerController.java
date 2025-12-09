package com.parcelhub.locker_gateway.controller;

import com.parcelhub.locker_gateway.dto.ReadyToPickupResponseDto;
import com.parcelhub.locker_gateway.dto.ShipmentInfo;
import com.parcelhub.locker_gateway.dto.request.PickupRequest;
import com.parcelhub.locker_gateway.service.LockerService;
import com.parcelhub.locker_gateway.dto.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("lockers")
public class LockerController {

    private final LockerService lockerService;

    public LockerController(LockerService lockerService) {
        this.lockerService = lockerService;
    }

    @GetMapping("/shipments/{id}")
    public ShipmentInfo getShipment(@PathVariable UUID id) {
        return lockerService.getShipmentInfo(id.toString());
    }

    @PostMapping("/{lockerId}/shipments/{shipmentId}/drop-off")
    public ResponseEntity<ResponseDto> dropOff(@PathVariable("lockerId") String lockerId,
                                               @PathVariable UUID shipmentId,
                                               @RequestHeader(value = "traceId", required = false) String traceId,
                                               @RequestHeader(value = "correlationId", required = false) String correlationId) {
        ResponseDto responseDto = lockerService.dropOff(lockerId, shipmentId, traceId, correlationId);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/{lockerId}/shipments/{shipmentId}/deliver")
    public ResponseEntity<ReadyToPickupResponseDto> deliver(@PathVariable("lockerId") String lockerId,
                                                            @PathVariable UUID shipmentId,
                                                            @RequestHeader(value = "traceId", required = false) String traceId,
                                                            @RequestHeader(value = "correlationId", required = false) String correlationId) {
        lockerService.deliveredToLocker(shipmentId, lockerId, traceId, correlationId);
        ReadyToPickupResponseDto readyToPickupResponseDto = lockerService.readyToPickup(lockerId, shipmentId, traceId, correlationId);
        return ResponseEntity.ok(readyToPickupResponseDto);
    }

    @PostMapping("/{lockerId}/shipments/{shipmentId}/pickup")
    public ResponseEntity<ResponseDto> pickup(@PathVariable("lockerId") String lockerId,
                                              @PathVariable UUID shipmentId,
                                              @RequestBody PickupRequest request,
                                              @RequestHeader(value = "traceId", required = false) String traceId,
                                              @RequestHeader(value = "correlationId", required = false) String correlationId) {
        ResponseDto responseDto = lockerService.pickupConfirmed(shipmentId, lockerId, request.pickupCode(), traceId, correlationId);
        return ResponseEntity.ok(responseDto);
    }
}
