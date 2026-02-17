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

    @PrePersist
    void prePersist() {
        if (processedAt == null) processedAt = Instant.now();
    }
}
