curl -sS -X POST http://localhost:8083/connectors \
  -H 'Content-Type: application/json' -d '{
    "name": "pg-outbox",
    "config": {
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
          "snapshot.mode": "initial",

          "transforms": "outbox,setShipCreated,setReturnInitiated,toOneTopic",
          "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
          "transforms.outbox.route.by.field": "event_type",
          "transforms.outbox.route.topic.regex": "(.*)",
          "transforms.outbox.route.topic.replacement": "server1-$1",

          "transforms.outbox.table.field.event.key": "aggregate_id",
          "transforms.outbox.table.field.payload": "payload",
          "transforms.outbox.table.field.timestamp": "created_at",
          "transforms.outbox.table.expand.json.payload": "true",
          "transforms.outbox.table.fields.additional.placement": "event_type:header,headers:header",

          "transforms.setShipCreated.type": "org.apache.kafka.connect.transforms.SetSchemaMetadata$Value",
          "transforms.setShipCreated.predicate": "isCreated",
          "transforms.setShipCreated.schema.name": "com.parcelhub.shipment.ShipmentCreated",
          "transforms.setReturnInitiated.type": "org.apache.kafka.connect.transforms.SetSchemaMetadata$Value",
          "transforms.setReturnInitiated.predicate": "isReturn",
          "transforms.setReturnInitiated.schema.name": "com.parcelhub.shipment.ReturnInitiated",

          "predicates": "isCreated,isReturn",
          "predicates.isCreated.type": "org.apache.kafka.connect.transforms.predicates.TopicNameMatches",
          "predicates.isCreated.pattern": ".*-ShipmentCreated$",
          "predicates.isReturn.type": "org.apache.kafka.connect.transforms.predicates.TopicNameMatches",
          "predicates.isReturn.pattern": ".*-ReturnInitiated$",

          "transforms.toOneTopic.type": "org.apache.kafka.connect.transforms.RegexRouter",
          "transforms.toOneTopic.regex": "^server1-.*$",
          "transforms.toOneTopic.replacement": "shipment-events",

          "key.converter": "org.apache.kafka.connect.storage.StringConverter",
          "value.converter": "io.apicurio.registry.utils.converter.AvroConverter",
          "value.converter.apicurio.registry.url": "http://apicurio:8080/apis/registry/v2",
          "value.converter.apicurio.registry.artifact.strategy": "io.apicurio.registry.utils.serde.strategy.TopicRecordIdStrategy",
          "value.converter.apicurio.registry.artifact.resolver.strategy": "io.apicurio.registry.utils.serde.strategy.TopicRecordIdStrategy",
          "value.converter.apicurio.registry.artifact.group-id": "shipment",
          "value.converter.apicurio.registry.use.headers": "true",
          "value.converter.apicurio.registry.use-id": "globalId"
    }
  }'