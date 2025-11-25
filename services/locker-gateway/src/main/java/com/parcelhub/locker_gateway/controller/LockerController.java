package com.parcelhub.locker_gateway.controller;

import com.parcelhub.locker_gateway.service.LockerService;
import com.parcelhub.locker_gateway.dto.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RestController
@RequestMapping("lockers")
public class LockerController {

    private final LockerService lockerService;

    public LockerController(LockerService lockerService) {
        this.lockerService = lockerService;
    }

    @PostMapping("/{lockerId}/shipments/{shipmentId}/drop-off")
    public ResponseEntity<ResponseDto> dropOff(@PathVariable("lockerId") String lockerId, @PathVariable UUID shipmentId) {
        ResponseDto responseDto = lockerService.dropOff(lockerId, shipmentId);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/{lockerId}/shipments/{shipmentId}/deliver")
    public void deliver(@PathVariable("lockerId") String lockerId, @PathVariable UUID shipmentId) {}

    @PostMapping("/{lockerId}/shipments/{shipmentId}/pickup")
    public ResponseEntity<ResponseDto> pickup(@PathVariable("lockerId") String lockerId, @PathVariable UUID shipmentId) {
        ResponseDto responseDto = lockerService.pickupConfirmed(shipmentId, lockerId);
        return ResponseEntity.ok(responseDto);
    }
}
