# ParcelHub — paczkomaty E2E (Java + Kafka)

> **Cel projektu:** Zbudować „CV-ready” system symulujący InPost – pełny lifecycle przesyłki od utworzenia etykiety po odbiór i rozliczenie – z naciskiem na praktyczne użycie **Apache Kafka** (producers/consumers, consumer groups, retry/DLQ, kompaktowanie, **Kafka Streams**, **CDC/Outbox**, transakcje EOS) oraz dobre praktyki mikroserwisowe.

## Spis treści
- [Założenia biznesowo-techniczne](#założenia-biznesowo-techniczne)
- [Architektura i serwisy](#architektura-i-serwisy)
- [Kluczowe funkcjonalności Kafki](#kluczowe-funkcjonalności-kafki)
- [Katalog tematów Kafka](#katalog-tematów-kafka)
- [Kontrakty danych](#kontrakty-danych)
- [Scenariusz demo (bez frontu)](#scenariusz-demo-bez-frontu)
- [Wymagania środowiskowe](#wymagania-środowiskowe)
- [Struktura repozytorium](#struktura-repozytorium)
- [Testy i jakość](#testy-i-jakość)
- [SLA/NFR](#slanfr)
- [Co trafia do CV](#co-trafia-do-cv)

---

## Założenia biznesowo-techniczne
- Symulacja sieci paczkomatów i hubów logistycznych: **drop-off → sortownia → locker docelowy → odbiór**.
- Brak front-endu aplikacyjnego; interakcje przez **REST API** + skrypty/`curl`.
- „Źródło prawdy” dla tworzenia przesyłki utrzymywane w **PostgreSQL** (moduł `shipment-api`) z **Transactional Outbox** → **Debezium** (CDC) → Kafka.
- Śledzenie statusu w czasie rzeczywistym przez **Kafka Streams** (KTable **compact** `shipment-tracking`).
- Powiadomienia z **retry/DLQ** i politykami opóźnień; rozliczenia po odbiorze.
- Pełna **observability** (metryki, trace, logi strukturalne) – panele operacyjne są opcjonalne.

---

## Architektura i serwisy

```
   [client/CLI]
        |  REST
        v
  +-----------------+        CDC/Outbox          +-------------------+
  |  shipment-api   |--------------------------->|   Kafka cluster    |
  |  (Postgres)     |                           | (topics & streams) |
  +-----------------+                           +-------------------+
          |                                                  |
          | REST                                             |
          v                                                  v
  +-----------------+     scans       +----------------+   (Streams)   +-----------------+
  | locker-gateway  |---------------->|  scan topics   |-------------->|     tracking    |
  +-----------------+                 +----------------+               +-----------------+
          ^                                  ^                                  |
          | REST                              | scans                           |
  +-----------------+                         |                                 v
  |  courier-app    |-------------------------+                         +-----------------+
  +-----------------+                                                   |  notification   |
                                                                        +-----------------+
                                                                                |
                                                                                v
                                                                        +-----------------+
                                                                        |     billing     |
                                                                        +-----------------+
```

**Mikroserwisy (bez frontu):**
- **`shipment-api`** — rejestr przesyłek, etykiety, zwroty. Produkuje `ShipmentCreated` / `ReturnInitiated` (Outbox+CDC).
- **`tracking`** — **Kafka Streams**: buduje bieżący status (`shipment-tracking`, compact) i publikuje `tracking-updates`.
- **`locker-gateway`** — symulator paczkomatu: `drop-off` / `deliver` / `pickup`. Produkuje skany i statusy.
- **`courier-app`** — symulator kuriera/hubów: `collect` / `hub-arrival` (skany i statusy).
- **`sortation`** — decyzje trasowania po skanie w hubie (Streams + `routing-table`, compact).
- **`notification`** — niezawodne powiadomienia (`retry` + `DLQ`).
- **`billing`** — rozliczenia po `PickupConfirmed` (eventy `Charge*`).
- **`reporting`** *(opcjonalny)* — raporty SLA/wolumeny (z eventów/CDC; może używać OpenSearch).

Dla każdej usługi istnieje „karta” w `/docs` (lub w osobnym pliku specyfikacji) opisująca rolę, API, eventy, dane i zależności.

---

## Kluczowe funkcjonalności Kafki
- **Transactional Outbox + Debezium (CDC)** – spójne publikowanie eventów domenowych.
- **Exactly-once**: producenci z `transactional.id`; **Kafka Streams** z `exactly_once_v2`.
- **Kompaktowanie**: rejestry (`locker-registry`, `routing-table`) i stan bieżący (`shipment-tracking`).
- **Retry/DLQ**: wielopoziomowe retry topics w `notification`; nagłówki `retryCount`.
- **Stream processing**: agregacja statusów, joiny z GlobalKTable, topologie z oknami gdzie potrzebne.
- **Schema registry**: Avro, kompatybilność **BACKWARD**, wersjonowanie kontraktów.

---

## Katalog tematów Kafka
*(opisowy – bez komend)*

**Domenowe (delete):** `shipment-events`, `scan-events.locker`, `scan-events.hub`, `tracking-updates`, `billing-events`  
**Stan (compact):** `shipment-tracking`  
**Rejestry (compact):** `locker-registry`, `routing-table`  
**Notyfikacje (delete + retry + DLQ):** `notification-requests`, `notification-requests.retry.5s|1m|10m`, `notification-requests.dlq`

- **Klucz partycjonowania:** `shipmentId` (dla zdarzeń przesyłki), `lockerId` (dla rejestru paczkomatów), `routeKey` dla `routing-table`.
- **Nagłówki standardowe:** `eventType`, `schemaVersion`, `traceId`, `correlationId`, (w `notification` także `retryCount`).

---

## Kontrakty danych
- **Schematy Avro** (storage w Apicurio Registry), polityka **BACKWARD**.
- Przykłady (skrócone):
  - `ShipmentCreated`: `{ eventId, shipmentId, sender{...}, recipient{...}, destinationLockerId, createdAt }`
  - `DropOffRegistered`: `{ eventId, shipmentId, lockerId, ts }`
  - `ReadyForPickup`: `{ eventId, shipmentId, lockerId, pickupCode, ts }`
  - `ShipmentTrackingState` (compact): `{ shipmentId, status, lastUpdate, lastLocation{type,id?}, destinationLockerId, version }`
  - `TrackingUpdated`: `{ eventId, shipmentId, oldStatus?, newStatus, changedAt, location{...} }`

Szczegóły per event i tabele – patrz specyfikacja serwisów (`/docs` lub plik specyfikacji mikroserwisów).

---

## Scenariusz demo (bez frontu)
Poniżej pełny „happy path” do ręcznego sprawdzenia. **Adresy/porty** są przykładowe.

1. **Utwórz przesyłkę** (`shipment-api`):
```bash
curl -H "X-API-Key: dev" -H "Content-Type: application/json" \
  -d '{
        "sender": {"name":"Jan Kowalski","phone":"+48501111222","zip":"30-001"},
        "recipient": {"name":"Anna Nowak","phone":null,"zip":"00-001"},
        "destinationLockerId":"KR123",
        "clientRequestId":"11111111-2222-3333-4444-555555555555"
      }' \
  http://localhost:8080/shipments
```
2. **Drop-off** w paczkomacie (`locker-gateway`):
```bash
curl -H "X-API-Key: dev" -H "Content-Type: application/json" \
  -d '{"shipmentId":"<ID_Z_KROKU_1>"}' \
  http://localhost:8081/lockers/KR001/drop-off
```
3. **Kurier odbiera** i **przyjeżdża na hub** (`courier-app`):
```bash
curl -H "X-API-Key: dev" -H "Content-Type: application/json" \
  -d '{"shipmentId":"<ID_Z_KROKU_1>","lockerId":"KR001"}' \
  http://localhost:8082/couriers/C1/collect

curl -H "X-API-Key: dev" -H "Content-Type: application/json" \
  -d '{"shipmentId":"<ID_Z_KROKU_1>","hubId":"HUB-KRK"}' \
  http://localhost:8082/couriers/C1/hub-arrival
```
4. **Sortowanie** (`sortation`) zdecyduje o docelowym punkcie; paczka trafia do **docelowego paczkomatu**.
5. **Gotowa do odbioru** i **odbiór** (`locker-gateway`):
```bash
# gotowe do odbioru → (symulacja deliver)
curl -H "X-API-Key: dev" -H "Content-Type: application/json" \
  -d '{"shipmentId":"<ID_Z_KROKU_1>","lockerId":"KR123"}' \
  http://localhost:8081/lockers/KR123/deliver

# odbiór
curl -H "X-API-Key: dev" -H "Content-Type: application/json" \
  -d '{"shipmentId":"<ID_Z_KROKU_1>","pickupCode":"1234"}' \
  http://localhost:8081/lockers/KR123/pickup
```
6. **Sprawdź tracking** (`tracking`):
```bash
curl -H "X-API-Key: dev" http://localhost:8083/shipments/<ID_Z_KROKU_1>/tracking
```

Dodatkowe scenariusze: zwrot po nieodebraniu, błędy w notyfikacjach (retry → DLQ), re‑processing trackingu.

---

## Wymagania środowiskowe
- **Docker** i **Docker Compose** (uruchomienie klastra Kafka + Postgres + ewentualne narzędzia).
- **Java 21**, **Maven** (build serwisów). 
- **Schema Registry (Apicurio)**, **Kafka Connect (Debezium)** – uruchomione w infrastrukturze.
- **Brak front-endu** – panele operacyjne (Grafana/Jaeger/Kafdrop) są opcjonalne.

> Świadomie brak tu plików `docker-compose` oraz YAML/ENV aplikacji – to jest w gestii dewelopera.

---

## Struktura repozytorium
```
parcelhub/
  README.md
  docs/
    microservices-spec.md        # karty serwisów (ten dokument lub nowszy)
  schemas/
    avro/ ...                    # kontrakty Avro
  infra/
    docker-compose.yml           # (infra: Kafka, Registry, Connect, Postgres, opcjonalne narzędzia)
    connectors/                  # definicje connectorów Debezium
  services/
    shipment-api/
    tracking/
    locker-gateway/
    courier-app/
    sortation/
    notification/
    billing/
  test-e2e/                      # testy E2E (Testcontainers)
```

---

## Testy i jakość
- **Unit** – logika domenowa/walidacje.
- **Contract** – zgodność schematów Avro (kompatybilność **BACKWARD** w Registry).
- **Integracyjne** – Outbox→CDC→Kafka, retry/DLQ (symulacja błędów providerów), idempotencja.
- **Streams** – testy topologii (TopologyTestDriver) dla trackingu i sortation.
- **E2E** – pełny flow „create → drop-off → hub → deliver → ready → pickup → billing”.

---

## SLA/NFR
- **Dostępność** REST dla krytycznych usług: **≥ 99%**.
- **Publikacja** `ShipmentCreated`: P95 **≤ 500 ms** (od zapisu do outbox do pojawienia się w Kafce).
- **Tracking update** widoczny: P95 **≤ 5 s** od zdarzenia.
- **Powiadomienia** po `ReadyForPickup`: P95 **≤ 60 s** pierwsza próba; retry z eskalacją do DLQ.
- **Bezpieczeństwo**: brak wrażliwych danych poza minimum; logi strukturalne, śledzenie `traceId`.

---

## Co trafia do CV
- Architektura event-driven (Java 21, Spring Boot 3, Kafka), 7 serwisów, **CDC/Outbox**, **Kafka Streams** (KTable compact), **retry/DLQ**, **exactly-once**, **Schema Registry**, testy E2E (Testcontainers), observability (OpenTelemetry, Prometheus/Grafana, Jaeger). Uruchamiane na Docker Compose.

---

# Specyfikacja mikroserwisów – ParcelHub (bez YAML/ENV, bez runbooka)

> Dokument zawiera „karty serwisów” dla całej architektury (bez plików Compose i bez konfiguracji aplikacyjnej YAML/ENV). Dla każdego serwisu: rola, odpowiedzialności, interfejsy (REST/Kafka), struktury danych (tabele / KTable / indeksy), wymagania tematów Kafka, zależności zewnętrzne, niezawodność/idempotencja oraz NFR/SLO.

---

## 0) Wspólne założenia i komponenty
- **Język/Framework:** Java 21, Spring Boot 3.x, Spring for Apache Kafka, Kafka Streams.
- **Kafka:** KRaft, ≥2 brokery, `min.insync.replicas=2`.
- **Serde:** Avro + Apicurio Registry (kompatybilność BACKWARD).
- **Connect/CDC:** Debezium z Outbox Event Router (tam gdzie DB → eventy).
- **Obserwowalność:** OpenTelemetry (traces), Micrometer → Prometheus (metryki), strukturalne logi (JSON). Panele operacyjne jak Grafana/Kafdrop/Jaeger są elementami operacyjnymi, nie frontendem produktu.
- **Bezpieczeństwo:** REST zabezpieczony `X-API-Key`. (Kafka ACL opcjonalnie.)
- **Klucz partycjonowania:** wszędzie `shipmentId` dla eventów przesyłek.

### Globalne tematy (wymagania – opis)
```yaml
# Tematy domenowe (delete)
shipment-events:
  partitions: 12
  replicationFactor: 2
  cleanupPolicy: delete
  retention: "7-14d"
  key: shipmentId
  headersRequired: [eventType, schemaVersion, traceId, correlationId, source]

scan-events.locker:
  partitions: 12
  replicationFactor: 2
  cleanupPolicy: delete
  retention: "14d"
  key: shipmentId

scan-events.hub:
  partitions: 12
  replicationFactor: 2
  cleanupPolicy: delete
  retention: "14d"
  key: shipmentId

# Stan bieżący (compact)
shipment-tracking:
  partitions: 12
  replicationFactor: 2
  cleanupPolicy: compact
  key: shipmentId

# Rejestry referencyjne (compact)
locker-registry:
  partitions: 3
  replicationFactor: 2
  cleanupPolicy: compact
  key: lockerId

routing-table:
  partitions: 3
  replicationFactor: 2
  cleanupPolicy: compact
  key: routeKey  # np. "originZip|destZip" lub inny klucz decyzyjny

# Notyfikacje (delete + retry + DLQ)
notification-requests:
  partitions: 6
  replicationFactor: 2
  cleanupPolicy: delete
  retention: "7d"
  key: businessKey

notification-requests.retry.5s / .1m / .10m:
  partitions: 6
  replicationFactor: 2
  cleanupPolicy: delete
  retention: "7d"

notification-requests.dlq:
  partitions: 6
  replicationFactor: 2
  cleanupPolicy: delete
  retention: "14d"

# Tracking wydarzeń (delete)
tracking-updates:
  partitions: 12
  replicationFactor: 2
  cleanupPolicy: delete
  retention: "7-14d"
  key: shipmentId

# Billing
billing-events:
  partitions: 6
  replicationFactor: 2
  cleanupPolicy: delete
  retention: "30d"
  key: shipmentId
```

---

## A) `shipment-api` — Rejestr przesyłek i źródło prawdy
**Rola:** Punkt wejścia do domeny przesyłek. Tworzy przesyłki, generuje metadane etykiety, inicjuje zwroty. Produkuje eventy domenowe poprzez **Transactional Outbox + Debezium**.

**Zakres:**
- Walidacja i zapis przesyłki (DB). 
- Emisja eventów `ShipmentCreated`, `ReturnInitiated` (z outbox/CDC).
- Metadane etykiety (`labelNumber`, `labelUrl`).

**Poza zakresem:** routing/sortowanie, tracking, notyfikacje, billing.

**REST (kontrakty):**
- `POST /shipments` → tworzy przesyłkę; nagłówek `X-API-Key`.  
  **Request:** sender/recipient/destinationLockerId/(opcjonalnie) clientRequestId  
  **Response 201:** `{ shipmentId, labelNumber, labelUrl, createdAt }`
- `POST /shipments/{shipmentId}/returns` → inicjuje zwrot.  
  **Request:** `{ reason }`  
  **Response 202:** `{ shipmentId, status: "RETURN_INITIATED" }`
- `GET /shipments/{shipmentId}/label` → metadane etykiety.

**Kafka (produkuje):** `shipment-events`  
- **Key:** `shipmentId`  
- **Headers:** `eventType`, `schemaVersion`, `traceId`, `correlationId`, `source=shipment-api`

**Eventy (wartości – przykłady JSON):**
- `ShipmentCreated`:
```json
{
  "eventId": "0f7f36b1-1e21-4a7a-9d4f-6a9b3f1c8a77",
  "shipmentId": "7e0f5f1f-7e9c-4d6b-8a89-2d1a5c9f2b10",
  "sender": { "name": "Jan Kowalski", "phone": "+48501111222", "zip": "30-001" },
  "recipient": { "name": "Anna Nowak", "phone": null, "zip": "00-001" },
  "destinationLockerId": "KR123",
  "createdAt": 1758445200000
}
```
- `ReturnInitiated`:
```json
{
  "eventId": "3e7150f6-8b3f-4b0f-92f1-9d1f3d30f0a1",
  "shipmentId": "7e0f5f1f-7e9c-4d6b-8a89-2d1a5c9f2b10",
  "reason": "NOT_PICKED_UP",
  "ts": 1758456000000
}
```

**Dane w DB (tabele):**
- `shipment.shipment` — rejestr przesyłek.  
  **Pola:** `id (UUID)`, `createdAt`, `updatedAt`, `status (CREATED|RETURN_INITIATED)`, `senderName`, `senderPhone?`, `senderZip`, `recipientName`, `recipientPhone?`, `recipientZip`, `destinationLockerId`, `labelNumber?`, `labelUrl?`, `clientRequestId? (UUID, unique)`.
  **Przykład YAML:**
```yaml
id: "7e0f5f1f-7e9c-4d6b-8a89-2d1a5c9f2b10"
createdAt: "2025-09-21T09:00:00Z"
updatedAt: "2025-09-21T09:00:00Z"
status: "CREATED"
senderName: "Jan Kowalski"
senderPhone: "+48501111222"
senderZip: "30-001"
recipientName: "Anna Nowak"
recipientPhone: null
recipientZip: "00-001"
destinationLockerId: "KR123"
labelNumber: "LBL-2025-000001"
labelUrl: "https://labels.local/LBL-2025-000001.pdf"
clientRequestId: "11111111-2222-3333-4444-555555555555"
```
- `shipment.outbox_event` — transactional outbox.  
  **Pola:** `id (UUID)`, `aggregateId (UUID)`, `aggregateType="shipment"`, `eventType`, `payload (JSON)`, `headers (JSON)`, `createdAt`.  
  **Przykład JSON:**
```json
{
  "id": "0f7f36b1-1e21-4a7a-9d4f-6a9b3f1c8a77",
  "aggregateId": "7e0f5f1f-7e9c-4d6b-8a89-2d1a5c9f2b10",
  "aggregateType": "shipment",
  "eventType": "ShipmentCreated",
  "payload": { /* jak wyżej */ },
  "headers": { "traceId": "...", "correlationId": "...", "schemaVersion": "1", "source": "shipment-api" },
  "createdAt": "2025-09-21T09:00:05Z"
}
```

**Zależności zewnętrzne:** Postgres (schema `shipment`), Kafka, Apicurio, Debezium (Outbox dla `shipment.outbox_event`), topic `shipment-events` istnieje.

**Niezawodność/Idempotencja:** transactional outbox; `clientRequestId` dla idempotencji REST; `eventId` w payloadzie.

**NFR/SLO:** dostępność REST ≥ 99%; publikacja `ShipmentCreated` P95 ≤ 500 ms od zapisu do outbox.

---

## B) `tracking` — Agregacja statusu i oś czasu
**Rola:** Buduje bieżący status przesyłki (KTable `shipment-tracking`) i emituje `tracking-updates` przy zmianach.

**Zakres:**
- Konsumpcja `shipment-events` (i/lub `scan-events.*`).
- Redukcja do stanu bieżącego per `shipmentId`.
- Emisja `tracking-updates` dla istotnych zmian.

**REST:** `GET /shipments/{id}/tracking` → `{ status, lastUpdate, lastLocation, destinationLockerId, version }`.

**Kafka (konsumuje):** `shipment-events` (key=`shipmentId`).

**Kafka (produkuje):**
- `shipment-tracking` (compact KTable; key=`shipmentId`; value=`ShipmentTrackingState`).
- `tracking-updates` (event o zmianie; key=`shipmentId`).

**Wartość `ShipmentTrackingState` (JSON) – pola:**
- `shipmentId (string)`, `status (enum)`, `lastUpdate (epochMillis)`,
- `lastLocation { type: LOCKER|HUB|NONE, id?: string }`,
- `destinationLockerId (string)`, `version (int)`.

**Przykład (JSON):**
```json
{
  "shipmentId": "7e0f5f1f-7e9c-4d6b-8a89-2d1a5c9f2b10",
  "status": "READY_FOR_PICKUP",
  "lastUpdate": 1758452400000,
  "lastLocation": { "type": "LOCKER", "id": "KR123" },
  "destinationLockerId": "KR123",
  "version": 6
}
```

**Event `TrackingUpdated` (JSON – wartości):**
```json
{
  "eventId": "2c53d0e8-8b4d-4a35-9cd9-1a665a7a0b1f",
  "shipmentId": "7e0f5f1f-7e9c-4d6b-8a89-2d1a5c9f2b10",
  "oldStatus": "DELIVERED_TO_LOCKER",
  "newStatus": "READY_FOR_PICKUP",
  "changedAt": 1758452400000,
  "location": { "type": "LOCKER", "id": "KR123" }
}
```

**Zależności:** Kafka, Apicurio; istnieją tematy `shipment-events`, `shipment-tracking`, `tracking-updates`.

**Niezawodność:** Kafka Streams `exactly_once_v2`; agregacja idempotentna (ostatni wygrywa po czasie/wersji).

**NFR/SLO:** update stanu widoczny ≤ 5 s P95; spójność docelowa (KTable compact).

---

## C) `locker-gateway` — Symulator paczkomatu
**Rola:** Reprezentuje paczkomat; rejestruje drop-off, depozyt i odbiór. Generuje skany i statusy.

**Zakres:**
- Walidacja minimalna (`shipmentId`, `lockerId`).
- Emisja `DropOffRegistered`, `DeliveredToLocker`, `ReadyForPickup`, `PickupConfirmed`.
- Generacja `pickupCode` (jednorazowy).

**REST:**
- `POST /lockers/{lockerId}/drop-off` → rejestr ujawnienia przesyłki.
- `POST /lockers/{lockerId}/deliver` → depozyt w skrytce; status gotowości.
- `POST /lockers/{lockerId}/pickup` → potwierdzenie odbioru.

**Kafka (produkuje):** `scan-events.locker` (skany) oraz `shipment-events` (statusy wynikowe).

**Przykładowe wartości eventów (JSON):**
- `DropOffRegistered`:
```json
{ "eventId": "...", "shipmentId": "...", "lockerId": "KR001", "ts": 1758448800000 }
```
- `DeliveredToLocker`:
```json
{ "eventId": "...", "shipmentId": "...", "lockerId": "KR123", "ts": 1758451200000 }
```
- `ReadyForPickup`:
```json
{ "eventId": "...", "shipmentId": "...", "lockerId": "KR123", "pickupCode": "1234", "ts": 1758451205000 }
```
- `PickupConfirmed`:
```json
{ "eventId": "...", "shipmentId": "...", "lockerId": "KR123", "ts": 1758462000000 }
```

**Rejestry referencyjne (konsumuje opcjonalnie):** `locker-registry` (compact) → walidacja `lockerId`.

**Zależności:** Kafka, Apicurio; istnienie `shipment-events`, `scan-events.locker`, (opcjonalnie) `locker-registry`.

**Niezawodność:** Idempotencja operacji (powtórne wywołanie nie duplikuje statusu); korelacja `traceId`.

**NFR/SLO:** opóźnienie od REST do eventu ≤ 300 ms P95.

---

## D) `courier-app` — Symulator kuriera i hubów
**Rola:** Symuluje odbiory z paczkomatów i dostawy do hubów; generuje skany kurier/hub.

**Zakres:**
- Emisja `CollectedFromLocker` po odbiorze.
- Emisja `ArrivedAtHub` po dotarciu do hubu.

**REST:**
- `POST /couriers/{courierId}/collect` → odbiór z paczkomatu.
- `POST /couriers/{courierId}/hub-arrival` → skan na hubie.

**Kafka (produkuje):** `scan-events.locker` (`Collected`), `scan-events.hub` (`ArrivedAtHub`), oraz odpowiednie `shipment-events` (statusy).

**Eventy (JSON – przykłady):**
- `CollectedFromLocker`:
```json
{ "eventId": "...", "shipmentId": "...", "lockerId": "KR001", "courierId": "C1", "ts": 1758449700000 }
```
- `ArrivedAtHub`:
```json
{ "eventId": "...", "shipmentId": "...", "hubId": "HUB-KRK", "ts": 1758450300000 }
```

**Zależności:** Kafka, Apicurio; istnienie `scan-events.*`, `shipment-events`; (opcjonalnie) `routing-table` do podpowiedzi.

**Niezawodność:** Idempotencja po (`courierId`,`shipmentId`,`actionTs`).

**NFR/SLO:** publish ≤ 300 ms P95; zgodność eventów z kontraktami.

---

## E) `sortation` — Decyzje trasowania (Streams + rejestr tras)
**Rola:** Po skanie w hubie wyznacza kolejny krok (następny hub lub docelowy paczkomat) na podstawie `routing-table` (compact).

**Zakres:**
- Join `scan-events.hub` z `routing-table` (GlobalKTable).
- Emisja `SortedToNextHub` lub `DeliveredToLocker`.

**Kafka (konsumuje):** `scan-events.hub`, `routing-table` (compact; key=`routeKey`).

**Kafka (produkuje):** `shipment-events` (`SortedToNextHub` / `DeliveredToLocker`).

**Struktura rekordu `routing-table` (value JSON – przykład):**
```json
{
  "routeKey": "30-001|00-001",
  "fromHubId": "HUB-KRK",
  "decision": { "type": "NEXT_HUB", "toHubId": "HUB-WAW" },
  "updatedAt": 1758445200000
}
```
- `decision.type`: `NEXT_HUB` lub `TO_LOCKER` (`targetLockerId`).

**Eventy wynikowe (JSON – przykłady):**
- `SortedToNextHub`:
```json
{ "eventId": "...", "shipmentId": "...", "fromHubId": "HUB-KRK", "toHubId": "HUB-WAW", "ts": 1758450400000 }
```
- `DeliveredToLocker` (gdy ostatni odcinek):
```json
{ "eventId": "...", "shipmentId": "...", "lockerId": "KR123", "ts": 1758452000000 }
```

**Zależności:** Kafka, Apicurio; `scan-events.hub`, `routing-table`, `shipment-events`.

**Niezawodność:** Kafka Streams `exactly_once_v2`; fallback na `routing-failed` (alert/obsługa poza zakresem).

**NFR/SLO:** decyzja w < 500 ms P95; pokrycie regułami ≥ 95% testowego trafficu.

---

## F) `notification` — Niezawodne powiadomienia (retry/DLQ)
**Rola:** Wysyła komunikaty transakcyjne (np. „Gotowe do odbioru”) z polityką retry z opóźnieniami i DLQ.

**Zakres:**
- Detekcja zdarzeń notyfikowalnych (np. `READY_FOR_PICKUP`).
- Budowa i wysyłka `NotificationRequest` do providerów (symulowanych).
- Retry topics (5s/1m/10m) i DLQ.

**Kafka (konsumuje):** `tracking-updates` **lub** `shipment-events` (filtr na `ReadyForPickup`).

**Kafka (produkuje):** `notification-requests`, `notification-requests.retry.*`, `notification-requests.dlq`, (opcjonalnie) `delivery-receipts`.

**Struktura `NotificationRequest` (JSON – przykład):**
```json
{
  "eventId": "...",
  "channel": "SMS",
  "recipient": "+48501111222",
  "templateId": "ready_for_pickup_pl",
  "variables": { "shipmentId": "...", "lockerId": "KR123", "pickupCode": "1234" },
  "businessKey": "READY_FOR_PICKUP:7e0f5f1f-7e9c-4d6b-8a89-2d1a5c9f2b10",
  "ts": 1758452400000
}
```

**(Opcjonalnie) Dedupe store – rekord (YAML):**
```yaml
id: "eventId"
processedAt: "2025-09-21T09:30:00Z"
```

**Zależności:** Kafka, Apicurio; istnieją retry/DLQ topics. 

**Niezawodność:** Idempotency Key = `businessKey`; `retryCount` w nagłówkach; limit prób np. 3.

**NFR/SLO:** czas dostarczenia pierwszego powiadomienia P95 ≤ 60 s od `READY_FOR_PICKUP`.

---

## G) `billing` — Rozliczenia po odbiorze
**Rola:** Naliczanie opłat po `PickupConfirmed`. Publikuje własne eventy billingowe.

**Zakres:**
- Reakcja na zdarzenia rozliczalne (np. `PickupConfirmed`).
- Tworzenie `invoice/charge` (DB) i emisja `billing-events` (`Charge*`).

**Kafka (konsumuje):** `shipment-events` (filtr na `PickupConfirmed`).

**Kafka (produkuje):** `billing-events`.

**Dane w DB (schema `billing`):**
- `billing.invoice` — uproszczony rejestr rozliczeń.  
  **Pola:** `id (UUID)`, `shipmentId (UUID)`, `amount (decimal)`, `currency (string)`, `status (REQUESTED|SUCCEEDED|FAILED)`, `createdAt`.
  **Przykład YAML:**
```yaml
id: "c1b6c67a-77d5-4a7a-a0a1-2f4a2c1a9b2d"
shipmentId: "7e0f5f1f-7e9c-4d6b-8a89-2d1a5c9f2b10"
amount: "12.99"
currency: "PLN"
status: "REQUESTED"
createdAt: "2025-09-21T12:00:00Z"
```

**Eventy billingowe (JSON – przykłady):**
- `ChargeRequested`:
```json
{ "eventId": "...", "shipmentId": "...", "amount": 12.99, "currency": "PLN", "ts": 1758465600000 }
```
- `ChargeSucceeded` / `ChargeFailed`:
```json
{ "eventId": "...", "shipmentId": "...", "provider": "mockpay", "authCode": "OK123", "ts": 1758465605000 }
```

**Zależności:** Postgres (schema `billing`), Kafka, Apicurio; `shipment-events` i `billing-events` istnieją.

**Niezawodność:** Idempotencja – brak podwójnych obciążeń dla tej samej pary (`shipmentId`,`PickupConfirmed`). (Opcjonalnie) Outbox + Debezium, jeżeli ma być EOS dla `billing-events`.

**NFR/SLO:** obciążenie utworzone ≤ 2 s P95 od `PickupConfirmed`.

---

## H) `reporting` (opcjonalny) — Raporty operacyjne i SLA
**Rola:** Zapewnia zagregowane widoki (SLA, wolumeny per paczkomat/hub).

**Zakres:**
- Konsumpcja `shipment-events`/`shipment-tracking` **lub** CDC z baz (`shipment`, `billing`).
- Utrzymywanie indeksów wyszukiwawczych (np. OpenSearch) lub agregatów w DB.

**Interfejsy:** `GET /reports/...` (okna czasowe, filtry). (Opcjonalne)

**Przykład dokumentu indeksu `shipment-timeline` (JSON):**
```json
{
  "shipmentId": "7e0f5f1f-7e9c-4d6b-8a89-2d1a5c9f2b10",
  "eventType": "ReadyForPickup",
  "ts": 1758452400000,
  "location": { "type": "LOCKER", "id": "KR123" },
  "source": "locker-gateway",
  "traceId": "...",
  "correlationId": "..."
}
```

**Zależności:** Kafka, Apicurio; (opcjonalnie) Connect/CDC; (opcjonalnie) OpenSearch.

**Niezawodność:** możliwość reprocessingu (reset offsetów) dla rekalkulacji raportów.

**NFR/SLO:** świeżość danych raportowych ≤ 1 min P95.

---

## 1) Zależności i wymagania zewnętrzne — matryca skrótowa

| Serwis           | Kafka | Registry | Postgres | Connect (Debezium) | Tematy wymagane (min.) | Dane referencyjne |
|------------------|:-----:|:--------:|:--------:|:-------------------:|------------------------|-------------------|
| shipment-api     |  ✅   |    ✅    |   ✅     |         ✅          | `shipment-events`      | –                 |
| tracking         |  ✅   |    ✅    |   –      |         –           | `shipment-events`, `shipment-tracking`, `tracking-updates` | `locker-registry` (opc.) |
| locker-gateway   |  ✅   |    ✅    |   –      |         –           | `shipment-events`, `scan-events.locker` | `locker-registry` (opc.) |
| courier-app      |  ✅   |    ✅    |   –      |         –           | `shipment-events`, `scan-events.hub`    | `routing-table` (opc.) |
| sortation        |  ✅   |    ✅    |   –      |         –           | `scan-events.hub`, `shipment-events`, `routing-table` | `routing-table` |
| notification     |  ✅   |    ✅    | (opc.)   |         –           | `tracking-updates`/`shipment-events`, `notification-requests*`, `.dlq` | – |
| billing          |  ✅   |    ✅    |   ✅     |       (opc.)        | `shipment-events`, `billing-events`     | –                 |
| reporting (opt.) |  ✅   |    ✅    | (opc.)   |       (opc.)        | `shipment-events`, `shipment-tracking`  | –                 |
