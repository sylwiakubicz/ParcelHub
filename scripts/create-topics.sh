#!/usr/bin/env bash
set -e
export PATH="/opt/bitnami/kafka/bin:$PATH"

echo "⏳ Czekam na uruchomienie brokera..."
sleep 10  # poczekaj, aż Kafka wystartuje

BOOTSTRAP="${BOOTSTRAP:-kafka-1:9092}"   # lub: localhost:19092

create_topic() {
  local TOPIC="$1"
  local PARTITIONS="$2"
  local REPLICAS="$3"
  local CLEANUP_POLICY="$4"
  local RETENTION_MS="$5"

  kafka-topics.sh \
    --create \
    --if-not-exists \
    --bootstrap-server "$BOOTSTRAP" \
    --topic "$TOPIC" \
    --partitions "$PARTITIONS" \
    --replication-factor "$REPLICAS"

  CFG="cleanup.policy=$CLEANUP_POLICY"
  if [[ -n "$RETENTION_MS" ]]; then
    CFG="$CFG,retention.ms=$RETENTION_MS"
  fi

  kafka-configs.sh \
      --bootstrap-server "$BOOTSTRAP" \
      --alter --entity-type topics --entity-name "$TOPIC" \
      --add-config "$CFG"

  kafka-configs.sh \
    --bootstrap-server "$BOOTSTRAP" \
    --alter --entity-type topics --entity-name "$TOPIC" \
    --add-config "min.insync.replicas=2"
}

create_topic shipment-events 12 3 delete 1209600000
create_topic tracking-updates 12 3 delete 1209600000
create_topic shipment-tracking 12 3 compact ""
create_topic connect-configs 1 3 compact ""
create_topic connect-offsets 12 3 compact ""
create_topic connect-status 6 3 compact ""


echo "✅ Topiki utworzone."