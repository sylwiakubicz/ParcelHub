-- 001-init.sql
CREATE SCHEMA IF NOT EXISTS shipment;


DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'shipment_status') THEN
CREATE TYPE shipment_status AS ENUM ('CREATED', 'RETURN_INITIATED');
END IF;
END$$;

CREATE TABLE IF NOT EXISTS shipment.shipment (
    id                UUID PRIMARY KEY,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    status            shipment_status NOT NULL,
    sender_name       TEXT NOT NULL,
    sender_phone      TEXT,
    sender_zip        TEXT NOT NULL,
    recipient_name    TEXT NOT NULL,
    recipient_phone   TEXT,
    recipient_zip     TEXT NOT NULL,
    destination_locker_id TEXT NOT NULL,
    label_number      TEXT,
    label_url         TEXT,
    client_request_id UUID UNIQUE
    );


CREATE OR REPLACE FUNCTION shipment.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at := now();
RETURN NEW;
END; $$ LANGUAGE plpgsql;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_trigger WHERE tgname = 'trg_set_updated_at'
  ) THEN
CREATE TRIGGER trg_set_updated_at
    BEFORE UPDATE ON shipment.shipment
    FOR EACH ROW EXECUTE FUNCTION shipment.set_updated_at();
END IF;
END$$;


CREATE INDEX IF NOT EXISTS idx_shipment_status ON shipment.shipment(status);
CREATE INDEX IF NOT EXISTS idx_shipment_dest_locker ON shipment.shipment(destination_locker_id);
