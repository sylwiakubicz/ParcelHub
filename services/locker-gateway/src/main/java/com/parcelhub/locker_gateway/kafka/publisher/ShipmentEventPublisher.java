package com.parcelhub.locker_gateway.kafka.publisher;

import com.parcelhub.locker_gateway.kafka.config.KafkaProperties;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ShipmentEventPublisher {
    private final KafkaProperties kafkaProperties;
    private final KafkaTemplate<String, SpecificRecord> kafkaTemplate;

    public ShipmentEventPublisher(KafkaProperties kafkaProperties, KafkaTemplate<String, SpecificRecord> kafkaTemplate) {
        this.kafkaProperties = kafkaProperties;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String key, SpecificRecord msg) {
        kafkaTemplate.send(kafkaProperties.getShipmentEvents(), key, msg);
    }
}
