CREATE SCHEMA IF NOT EXISTS lockers;

CREATE TABLE IF NOT EXISTS lockers.lockers (
    id               UUID PRIMARY KEY,
    shipment_id      UUID        NOT NULL,
    locker_id        VARCHAR(50) NOT NULL,
    pickup_code_hash VARCHAR(128) NOT NULL,
    generated_at     TIMESTAMPTZ NOT NULL,
    expires_at       TIMESTAMPTZ NULL,
    used_at          TIMESTAMPTZ NULL
    );

ALTER TABLE lockers.lockers
    ADD CONSTRAINT uq_lockers_shipment UNIQUE (shipment_id);