package com.parcelhub.locker_gateway.service;

import com.parcelhub.locker_gateway.client.HttpTrackingClient;
import com.parcelhub.locker_gateway.dto.ReadyToPickupResponseDto;
import com.parcelhub.locker_gateway.dto.ResponseDto;
import com.parcelhub.locker_gateway.dto.ShipmentInfo;
import com.parcelhub.locker_gateway.dto.ShipmentStatus;
import com.parcelhub.locker_gateway.exception.*;
import com.parcelhub.locker_gateway.kafka.publisher.ShipmentEventPublisher;
import com.parcelhub.locker_gateway.security.PickupCodeHasher;
import com.parcelhub.shipment.DeliveredToLocker;
import com.parcelhub.shipment.DropOffRegistered;
import com.parcelhub.shipment.PickupConfirmed;
import com.parcelhub.shipment.ReadyForPickup;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

@Service
public class LockerService {
    private final ShipmentEventPublisher shipmentEventPublisher;
    private final HttpTrackingClient httpTrackingClient;
    private final PickupCodeHasher pickupCodeHasher;
    private final LockerPickUpService lockerPickUpService;

    public LockerService(ShipmentEventPublisher shipmentEventPublisher, HttpTrackingClient httpTrackingClient,
                         PickupCodeHasher pickupCodeHasher, LockerPickUpService lockerPickUpService) {
        this.shipmentEventPublisher = shipmentEventPublisher;
        this.httpTrackingClient = httpTrackingClient;
        this.pickupCodeHasher = pickupCodeHasher;
        this.lockerPickUpService = lockerPickUpService;
    }

    private ResponseDto createResponseDto(UUID shipmentId, ShipmentStatus status) {
        return new ResponseDto(shipmentId.toString(), status);
    }

    public ShipmentInfo getShipmentInfo(String shipmentId) {
        return httpTrackingClient.getShipmentInfo(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));
    }

    public String generatePickupCode() {
        int i = new Random().nextInt(900000) + 100000;
        return Integer.toString(i);
    }

    public ResponseDto dropOff(String lockerId, UUID shipmentId) {
        getShipmentInfo(shipmentId.toString());

        DropOffRegistered dropOffRegistered = new DropOffRegistered();
        dropOffRegistered.setLockerId(lockerId);
        dropOffRegistered.setShipmentId(shipmentId);
        dropOffRegistered.setEventId(UUID.randomUUID());
        dropOffRegistered.setTs(Instant.now());

        shipmentEventPublisher.sendMessage(String.valueOf(shipmentId), dropOffRegistered);

        return createResponseDto(shipmentId, ShipmentStatus.DROPPED_OFF_AT_LOCKER);
    }

    public ResponseDto deliveredToLocker(UUID shipmentId, String lockerId) {
        ShipmentInfo info = getShipmentInfo(shipmentId.toString());

        if (!Objects.equals(info.getDestinationLockerId(), lockerId)) {
            throw new InvalidLockerId(lockerId);
        }

        DeliveredToLocker deliveredToLocker = new DeliveredToLocker();
        deliveredToLocker.setShipmentId(shipmentId);
        deliveredToLocker.setEventId(UUID.randomUUID());
        deliveredToLocker.setTs(Instant.now());
        deliveredToLocker.setLockerId(lockerId);

        shipmentEventPublisher.sendMessage(String.valueOf(shipmentId), deliveredToLocker);

        return createResponseDto(shipmentId, ShipmentStatus.DELIVERED_TO_LOCKER);
    }

    @Transactional
    public ReadyToPickupResponseDto readyToPickup(String lockerId, UUID shipmentId) {
        try {
            String code = generatePickupCode();
            String codeHash = pickupCodeHasher.hash(shipmentId, lockerId, code);

            lockerPickUpService.saveLocker(shipmentId, lockerId, codeHash);

            ReadyForPickup readyForPickup = new ReadyForPickup();
            readyForPickup.setLockerId(lockerId);
            readyForPickup.setShipmentId(shipmentId);
            readyForPickup.setEventId(UUID.randomUUID());
            readyForPickup.setTs(Instant.now());
            readyForPickup.setPickupCode(code);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    shipmentEventPublisher.sendMessage(shipmentId.toString(), readyForPickup);
                }
            });

            return new ReadyToPickupResponseDto(shipmentId.toString(), ShipmentStatus.READY_FOR_PICKUP, code);
        } catch (DataIntegrityViolationException e) {
            throw new ReadyToPickupProcessingException("There were some issue with setting shipment to ready to pickup");
        }
    }

    @Transactional
    public ResponseDto pickupConfirmed(UUID shipmentId, String lockerId, String pickupCode) {
        try {
            ShipmentInfo info = getShipmentInfo(shipmentId.toString());

            if (info.getStatus() != ShipmentStatus.READY_FOR_PICKUP) {
                throw new NotReadyToPickUp(shipmentId.toString());
            }

            if (!Objects.equals(info.getDestinationLockerId(), lockerId)) {
                throw new InvalidLockerId(lockerId);
            }

            String givenPickupCodeHash = pickupCodeHasher.hash(shipmentId, lockerId, pickupCode);
            String actualPickupCodeHash = lockerPickUpService.getPickupCodeHash(shipmentId, lockerId);
            if (!Objects.equals(actualPickupCodeHash, givenPickupCodeHash)) {
                throw new InvalidPickupCodeException("Wrong Pickup Code");
            }

            PickupConfirmed pickupConfirmed = new PickupConfirmed();
            pickupConfirmed.setShipmentId(shipmentId);
            pickupConfirmed.setEventId(UUID.randomUUID());
            pickupConfirmed.setTs(Instant.now());
            pickupConfirmed.setLockerId(lockerId);

            lockerPickUpService.updateLocker(shipmentId, lockerId);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    shipmentEventPublisher.sendMessage(String.valueOf(shipmentId), pickupConfirmed);
                }
            });

            return createResponseDto(shipmentId, ShipmentStatus.PICKED_UP);
        } catch (DataIntegrityViolationException e) {
            throw new ReadyToPickupProcessingException("There were some issue with setting shipment to ready to pickup");
        }
    }
}
