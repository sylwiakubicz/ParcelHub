package com.parcelhub.locker_gateway.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        schema = "lockers",
        name = "lockers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_lockers_shipment",
                        columnNames = "shipment_id"
                )
        }
)
public class Locker {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "shipment_id", nullable = false)
    private UUID shipmentId;

    @Column(name = "locker_id", nullable = false)
    private String lockerId;

    @Column(name = "pickup_code_hash", nullable = false)
    private String pickupCodeHash;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(UUID shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getLockerId() {
        return lockerId;
    }

    public void setLockerId(String lockerId) {
        this.lockerId = lockerId;
    }

    public String getPickupCodeHash() {
        return pickupCodeHash;
    }

    public void setPickupCodeHash(String pickupCodeHash) {
        this.pickupCodeHash = pickupCodeHash;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Instant usedAt) {
        this.usedAt = usedAt;
    }
}
