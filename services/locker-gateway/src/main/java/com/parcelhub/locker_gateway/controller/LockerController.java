package com.parcelhub.locker_gateway.controller;

import com.parcelhub.locker_gateway.dto.ShipmentInfo;
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
    public ShipmentInfo getShipment(@PathVariable String id) {
        return lockerService.getShipmentInfo(id);
    }

    @PostMapping("/{lockerId}/shipments/{shipmentId}/drop-off")
    public ResponseEntity<ResponseDto> dropOff(@PathVariable("lockerId") String lockerId, @PathVariable UUID shipmentId) {
        ResponseDto responseDto = lockerService.dropOff(lockerId, shipmentId);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/{lockerId}/shipments/{shipmentId}/deliver")
    public void deliver(@PathVariable("lockerId") String lockerId, @PathVariable UUID shipmentId) {}

    @PostMapping("/{lockerId}/shipments/{shipmentId}/pickup")
    public ResponseEntity<ResponseDto> pickup(@PathVariable("lockerId") String lockerId, @PathVariable UUID shipmentId,
                                              @RequestBody String pickupCode) {
        // todo walidacja kodu wstępna (6 cyfr)
        ResponseDto responseDto = lockerService.pickupConfirmed(shipmentId, lockerId, pickupCode);
        return ResponseEntity.ok(responseDto);
    }
}
