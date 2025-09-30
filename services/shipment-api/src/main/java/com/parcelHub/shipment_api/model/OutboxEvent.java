package com.parcelHub.shipment_api.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "shipment", name = "outbox_event")
public class OutboxEvent {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    // JSONB → Hibernate 6
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode payload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "headers", columnDefinition = "jsonb")
    private JsonNode headers;

    // w DB: DEFAULT now()
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;

    public OutboxEvent() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getAggregateId() { return aggregateId; }
    public void setAggregateId(UUID aggregateId) { this.aggregateId = aggregateId; }

    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public JsonNode getPayload() { return payload; }
    public void setPayload(JsonNode payload) { this.payload = payload; }

    public JsonNode getHeaders() { return headers; }
    public void setHeaders(JsonNode headers) { this.headers = headers; }

    public Instant getCreatedAt() { return createdAt; }
}
