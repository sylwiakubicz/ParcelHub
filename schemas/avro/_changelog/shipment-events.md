# Changelog – shipment-events

## [1.0.0] – 2025-10-01
**Typ zmiany:** Initial release (pierwsze opublikowanie schematów)  
**Kompatybilność:** BACKWARD (ustawiona w Registry) – brak zmian łamiących, punkt startowy

### Zakres wydania
- Temat: `shipment-events`
- Strategia artefaktów: `TopicRecordIdStrategy` → `<topic>-<recordName>`
- Namespace: `com.parcelhub.shipment.events`

### Artefakty dodane (Apicurio)
- **Key**
    - ArtifactId: `shipment-events-ShipmentKey`
    - Rekord: `ShipmentKey`
    - Pola:
        - `shipmentId` – `string` (`logicalType: uuid`)
- **Value**
    - ArtifactId: `shipment-events-ShipmentCreated`
    - Rekord: `ShipmentCreated`
    - Pola:
        - `eventId` – `string` (`logicalType: uuid`)
        - `shipmentId` – `string` (`logicalType: uuid`)
        - `createdAt` – `long` (`logicalType: timestamp-millis`)
        - `sender` – rekord `Person { name: string, phone: ["null","string"]=null, zip: string }`
        - `recipient` – `Person`
        - `destinationLockerId` – `string`
        - `labelNumber` – `["null","string"]=null`
        - `labelUrl` – `["null","string"]=null`
- **Value**
    - ArtifactId: `shipment-events-ReturnInitiated`
    - Rekord: `ReturnInitiated`
    - Pola:
        - `eventId` – `string` (`logicalType: uuid`)
        - `shipmentId` – `string` (`logicalType: uuid`)
        - `reason` – `string`
        - `ts` – `long` (`logicalType: timestamp-millis`)

### Nagłówki zdarzeń (Kafka headers)
Wykorzystywane przez producenta (Debezium Outbox → EventRouter), poza Avro:
- `eventType` (np. `ShipmentCreated`, `ReturnInitiated`)
- `schemaVersion` (np. `1.0.0`) – **informacyjne**, zgodne z tym changelogiem
- `traceId`, `correlationId`
- `source = shipment-api`

### Zasady kompatybilności
- **Global/Artifact rule:** `BACKWARD`
- Pola opcjonalne posiadają `["null","<typ>"]` z `default: null`, co ułatwia przyszłe bezpieczne rozszerzenia.

### Testy weryfikujące
- Serializacja przykładowych rekordów `ShipmentCreated` i `ReturnInitiated` w trybie Avro + Apicurio Registry (lokalnie).
- Konsument (mock) z Avro deserializerem poprawnie odczytuje rekordy z id schematu z Registry.
- Weryfikacja, że klucz `ShipmentKey` mapuje się do `shipmentId`.

### Notatki wdrożeniowe
- **Auto-rejestracja w Connect wyłączona** – artefakty zarejestrowane ręcznie przed uruchomieniem Debezium.
- Producent: Debezium Outbox z `TopicRecordIdStrategy` – zgodność nazw rekordów i artifactId zapewniona.
- Konsumenci: muszą używać Avro deserializera z dostępem do Apicurio.

### Pliki w repo
- `schemas/avro/shipment-events/key/ShipmentKey.avsc`
- `schemas/avro/shipment-events/value/ShipmentCreated.avsc`
- `schemas/avro/shipment-events/value/ReturnInitiated.avsc`

---