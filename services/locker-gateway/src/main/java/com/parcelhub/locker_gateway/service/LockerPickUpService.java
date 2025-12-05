package com.parcelhub.locker_gateway.service;

import com.parcelhub.locker_gateway.exception.ShipmentNotFoundException;
import com.parcelhub.locker_gateway.model.Locker;
import com.parcelhub.locker_gateway.repository.LockersRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class LockerPickUpService {

    private final LockersRepository lockersRepository;
    private static final long PICKUP_VALIDITY_SECONDS = 172800L; // 2 days

    public LockerPickUpService(LockersRepository lockersRepository) {
        this.lockersRepository = lockersRepository;
    }

    private Locker getLockerOrThrow(UUID shipmentId, String lockerId) {
        return lockersRepository.findByShipmentIdAndLockerId(shipmentId, lockerId)
                .orElseThrow(() -> new ShipmentNotFoundException(shipmentId.toString()));
    }

    public void saveLocker(UUID shipmentId, String lockerId, String pickupCodeHash) {

        // TODO czy potrzbne sprawdzanie że w bazie nie ma wpisu dla niego?
        Locker locker = new Locker();
        locker.setLockerId(lockerId);
        locker.setShipmentId(shipmentId);
        locker.setGeneratedAt(Instant.now());
        locker.setExpiresAt(Instant.now().plusSeconds(PICKUP_VALIDITY_SECONDS));
        locker.setPickupCodeHash(pickupCodeHash);
        locker.setId(UUID.randomUUID());

        lockersRepository.save(locker);
    }

    public String getPickupCodeHash(UUID shipmentId, String lockerId) {
        return getLockerOrThrow(shipmentId, lockerId).getPickupCodeHash();
    }

    public void updateLocker(UUID shipmentId, String lockerId) {
        Locker locker = getLockerOrThrow(shipmentId, lockerId);
        locker.setUsedAt(Instant.now());
        lockersRepository.save(locker);
    }
}
