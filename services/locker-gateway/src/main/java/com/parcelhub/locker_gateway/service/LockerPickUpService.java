package com.parcelhub.locker_gateway.service;

import com.parcelhub.locker_gateway.exception.ShipmentNotFoundException;
import com.parcelhub.locker_gateway.model.Locker;
import com.parcelhub.locker_gateway.repository.LockersRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class LockerPickUpService {

    private LockersRepository lockersRepository;

    public LockerPickUpService(LockersRepository lockersRepository) {
        this.lockersRepository = lockersRepository;
    }

    public void saveLocker(String shipmentId, String lockerId, String pickupCodeHash) {
        Locker locker = new Locker();
        locker.setLockerId(lockerId);
        locker.setShipmentId(UUID.fromString(shipmentId));
        locker.setGeneratedAt(Instant.now());
        // valid for two days (172800s)
        locker.setExpiresAt(Instant.now().plusSeconds(172800));
        locker.setPickupCodeHash(pickupCodeHash);
        locker.setId(UUID.randomUUID());

        lockersRepository.save(locker);
    }

    public String getPickupCodeHash(String shipmentId, String lockerId) {
        Locker locker = lockersRepository.findByShipmentIdAndLockerId(UUID.fromString(shipmentId), lockerId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));
        return locker.getPickupCodeHash();
    }

    public void updateLocker(String shipmentId, String lockerId) {
        Locker locker = lockersRepository.findByShipmentIdAndLockerId(UUID.fromString(shipmentId), lockerId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId));
        locker.setUsedAt(Instant.now());
        lockersRepository.save(locker);
    }
}
