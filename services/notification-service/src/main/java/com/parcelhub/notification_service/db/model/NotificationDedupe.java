package com.parcelhub.notification_service.db.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(schema="notifications", name="notification_dedupe")
public class NotificationDedupe {
    @Id
    @Column(nullable = false)
    private String businessKey;

    @Column(nullable = false)
    private UUID shipmentId;

    @Column(nullable = false)
    private Instant processedAt;

    public NotificationDedupe() {
    }

    public NotificationDedupe(String businessKey, UUID shipmentId) {
        this.businessKey = businessKey;
        this.shipmentId = shipmentId;
    }

    @PrePersist
    void prePersist() {
        if (processedAt == null) processedAt = Instant.now();
    }
}
