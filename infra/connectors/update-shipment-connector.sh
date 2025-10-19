curl -sS -X PUT http://localhost:8083/connectors/pg-outbox/config \
  -H 'Content-Type: application/json' -d '{
    "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
    "plugin.name": "pgoutput",

    "database.hostname": "postgres_db",
    "database.port": "5432",
    "database.user": "postgres",
    "database.password": "TU_HASLO",
    "database.dbname": "shipment",

    "topic.prefix": "server1",

    "schema.include.list": "shipment",
    "table.include.list": "shipment.outbox_event",
    "slot.name": "shipment_outbox_slot",
    "publication.autocreate.mode": "filtered",

    "transforms": "outbox",
    "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
    "transforms.outbox.route.by.field": "aggregate_type",
    "transforms.outbox.route.topic.regex": "(?<r>.*)",
    "transforms.outbox.route.topic.replacement": "${r}-events",
    "transforms.outbox.table.field.event.key": "aggregate_id",
    "transforms.outbox.table.field.payload": "payload",
    "transforms.outbox.table.field.timestamp": "created_at",
    "transforms.outbox.table.fields.additional.placement": "event_type:header,headers:header",
    "transforms.outbox.table.expand.json.payload": "true",

    "snapshot.mode": "initial",

    "key.converter": "org.apache.kafka.connect.storage.StringConverter",

    "value.converter": "io.apicurio.registry.utils.converter.AvroConverter",
    "value.converter.apicurio.registry.url": "http://apicurio:8080/apis/registry/v2",
    "value.converter.apicurio.registry.artifact.strategy": "io.apicurio.registry.utils.serde.strategy.TopicRecordIdStrategy",
    "value.converter.apicurio.registry.artifact.group-id": "shipment",
    "value.converter.apicurio.registry.use.headers": "true"
  }'
