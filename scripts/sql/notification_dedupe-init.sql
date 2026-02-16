CREATE SCHEMA IF NOT EXISTS notifications;

CREATE TABLE IF NOT EXISTS notifications.notification_dedupe (
            business_key     TEXT PRIMARY KEY,
            shipment_id      UUID NOT NULL,
            processed_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
