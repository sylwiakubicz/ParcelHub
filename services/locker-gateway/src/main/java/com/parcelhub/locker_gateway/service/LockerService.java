package com.parcelhub.locker_gateway.service;

import com.parcelhub.locker_gateway.client.HttpTrackingClient;
import com.parcelhub.locker_gateway.dto.ReadyToPickupResponseDto;
import com.parcelhub.locker_gateway.dto.ResponseDto;
import com.parcelhub.locker_gateway.dto.ShipmentInfo;
import com.parcelhub.locker_gateway.dto.ShipmentStatus;
import com.parcelhub.locker_gateway.exception.InvalidLockerId;
import com.parcelhub.locker_gateway.exception.NotReadyToPickUp;
import com.parcelhub.locker_gateway.exception.ShipmentNotFoundException;
import com.parcelhub.locker_gateway.kafka.publisher.ShipmentEventPublisher;
import com.parcelhub.locker_gateway.model.Locker;
import com.parcelhub.locker_gateway.repository.LockersRepository;
import com.parcelhub.locker_gateway.security.PickupCodeHasher;
import com.parcelhub.shipment.DeliveredToLocker;
import com.parcelhub.shipment.DropOffRegistered;
import com.parcelhub.shipment.PickupConfirmed;
import com.parcelhub.shipment.ReadyForPickup;
import org.springframework.stereotype.Service;

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

    public ResponseDto createResponseDto(UUID shipmentId, ShipmentStatus status) {
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

    public ReadyToPickupResponseDto readyToPickup(String lockerId, UUID shipmentId) {
        String code = generatePickupCode();
        String codeHash = pickupCodeHasher.hash(shipmentId, lockerId, code);

        Locker locker = lockerPickUpService.saveLocker(shipmentId.toString(), lockerId, codeHash);

        // todo transactional method
        if (locker != null) {
            ReadyForPickup readyForPickup = new ReadyForPickup();
            readyForPickup.setLockerId(lockerId);
            readyForPickup.setShipmentId(shipmentId);
            readyForPickup.setEventId(UUID.randomUUID());
            readyForPickup.setTs(Instant.now());
            readyForPickup.setPickupCode(code);

            shipmentEventPublisher.sendMessage(shipmentId.toString(), readyForPickup);

            return new ReadyToPickupResponseDto(shipmentId.toString(), ShipmentStatus.READY_FOR_PICKUP, code);
        }

        return null;
    }

    // todo it must get also pickup code
    public ResponseDto pickupConfirmed(UUID shipmentId, String lockerId, String pickupCode) {
        ShipmentInfo info = getShipmentInfo(shipmentId.toString());

        if (info.getStatus() != ShipmentStatus.READY_FOR_PICKUP) {
            throw new NotReadyToPickUp(shipmentId.toString());
        }

        if (!Objects.equals(info.getDestinationLockerId(), lockerId)) {
            throw new InvalidLockerId(lockerId);
        }

        // todo walidacja kodu odbioru
        String pickupCodeHash = pickupCodeHasher.hash(shipmentId, lockerId, pickupCode);


        PickupConfirmed pickupConfirmed = new PickupConfirmed();
        pickupConfirmed.setShipmentId(shipmentId);
        pickupConfirmed.setEventId(UUID.randomUUID());
        pickupConfirmed.setTs(Instant.now());
        pickupConfirmed.setLockerId(lockerId);

        shipmentEventPublisher.sendMessage(String.valueOf(shipmentId), pickupConfirmed);

        return createResponseDto(shipmentId, ShipmentStatus.PICKED_UP);
    }
}
