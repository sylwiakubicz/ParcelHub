package com.parcelhub.locker_gateway.service;

import com.parcelhub.locker_gateway.client.HttpTrackingClient;
import com.parcelhub.locker_gateway.dto.ReadyToPickupResponseDto;
import com.parcelhub.locker_gateway.dto.ResponseDto;
import com.parcelhub.locker_gateway.dto.ShipmentInfo;
import com.parcelhub.locker_gateway.dto.ShipmentStatus;
import com.parcelhub.locker_gateway.exception.*;
import com.parcelhub.locker_gateway.kafka.publisher.LockerKafkaPublisher;
import com.parcelhub.locker_gateway.security.PickupCodeHasher;
import com.parcelhub.shipment.DeliveredToLocker;
import com.parcelhub.shipment.DropOffRegistered;
import com.parcelhub.shipment.PickupConfirmed;
import com.parcelhub.shipment.ReadyForPickup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

@Service
public class LockerService {
    private final LockerKafkaPublisher lockerKafkaPublisher;
    private final HttpTrackingClient httpTrackingClient;
    private final PickupCodeHasher pickupCodeHasher;
    private final LockerPickUpService lockerPickUpService;

    public LockerService(LockerKafkaPublisher lockerKafkaPublisher, HttpTrackingClient httpTrackingClient,
                         PickupCodeHasher pickupCodeHasher, LockerPickUpService lockerPickUpService) {
        this.lockerKafkaPublisher = lockerKafkaPublisher;
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

    public ResponseDto dropOff(String lockerId, UUID shipmentId, String traceId, String correlationId)  {
        getShipmentInfo(shipmentId.toString());

        DropOffRegistered dropOffRegistered = new DropOffRegistered();
        dropOffRegistered.setLockerId(lockerId);
        dropOffRegistered.setShipmentId(shipmentId);
        dropOffRegistered.setEventId(UUID.randomUUID());
        dropOffRegistered.setTs(Instant.now());

        lockerKafkaPublisher.sendShipmentEvent(
                String.valueOf(shipmentId),
                dropOffRegistered,
                Map.of(
                "traceId", traceId,
                "correlationId", correlationId,
                "source", "locker-gateway",
                "event_type", "drop_off"
        ));

        lockerKafkaPublisher.sendScanEvent(
                String.valueOf(shipmentId),
                dropOffRegistered,
                Map.of(
                        "traceId", traceId,
                        "correlationId", correlationId,
                        "source", "locker-gateway",
                        "event_type", "drop_off"
                ));

        return createResponseDto(shipmentId, ShipmentStatus.DROPPED_OFF_AT_LOCKER);
    }

    public void deliveredToLocker(UUID shipmentId, String lockerId, String traceId, String correlationId) {
        ShipmentInfo info = getShipmentInfo(shipmentId.toString());

        if (!Objects.equals(info.getDestinationLockerId(), lockerId)) {
            throw new InvalidLockerId(lockerId);
        }

        DeliveredToLocker deliveredToLocker = new DeliveredToLocker();
        deliveredToLocker.setShipmentId(shipmentId);
        deliveredToLocker.setEventId(UUID.randomUUID());
        deliveredToLocker.setTs(Instant.now());
        deliveredToLocker.setLockerId(lockerId);

        lockerKafkaPublisher.sendShipmentEvent(
                String.valueOf(shipmentId),
                deliveredToLocker,
                Map.of(
                        "traceId", traceId,
                        "correlationId", correlationId,
                        "source", "locker-gateway",
                        "event_type", "delivered_to_locker"
                ));

        lockerKafkaPublisher.sendScanEvent(
                String.valueOf(shipmentId),
                deliveredToLocker,
                Map.of(
                        "traceId", traceId,
                        "correlationId", correlationId,
                        "source", "locker-gateway",
                        "event_type", "delivered_to_locker"
                )
        );
    }

    @Transactional
    public ReadyToPickupResponseDto readyToPickup(String lockerId, UUID shipmentId,
                                                  String traceId, String correlationId) {
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
                lockerKafkaPublisher.sendShipmentEvent(
                        shipmentId.toString(),
                        readyForPickup,
                        Map.of(
                                "traceId", traceId,
                                "correlationId", correlationId,
                                "source", "locker-gateway",
                                "event_type", "ready_for_pickup"
                        ));

                lockerKafkaPublisher.sendScanEvent(
                        shipmentId.toString(),
                        readyForPickup,
                        Map.of(
                                "traceId", traceId,
                                "correlationId", correlationId,
                                "source", "locker-gateway",
                                "event_type", "ready_for_pickup"
                        )
                );
            }
        });

        return new ReadyToPickupResponseDto(shipmentId.toString(), ShipmentStatus.READY_FOR_PICKUP, code);
    }

    @Transactional
    public ResponseDto pickupConfirmed(UUID shipmentId, String lockerId, String pickupCode,
                                       String traceId, String correlationId) {
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
                lockerKafkaPublisher.sendShipmentEvent(
                        String.valueOf(shipmentId),
                        pickupConfirmed,
                        Map.of(
                                "traceId", traceId,
                                "correlationId", correlationId,
                                "source", "locker-gateway",
                                "event_type", "pickup_confirmed"
                        ));

                lockerKafkaPublisher.sendScanEvent(
                        String.valueOf(shipmentId),
                        pickupConfirmed,
                        Map.of(
                                "traceId", traceId,
                                "correlationId", correlationId,
                                "source", "locker-gateway",
                                "event_type", "pickup_confirmed"
                        ));
            }
        });

        return createResponseDto(shipmentId, ShipmentStatus.PICKED_UP);
    }
}
