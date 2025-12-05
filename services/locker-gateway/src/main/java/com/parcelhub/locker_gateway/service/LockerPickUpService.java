package com.parcelhub.locker_gateway.service;

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

    public Locker saveLocker(String shipmentId, String lockerId, String pickupCodeHash) {
        Locker locker = new Locker();
        locker.setLockerId(lockerId);
        locker.setShipmentId(UUID.fromString(shipmentId));
        locker.setGeneratedAt(Instant.now());
        // valid for two days (172800s)
        locker.setExpiresAt(Instant.now().plusSeconds(172800));
        locker.setPickupCodeHash(pickupCodeHash);
        locker.setId(UUID.randomUUID());

        return lockersRepository.save(locker);
    }

    // TODO get pickup code hash
}
