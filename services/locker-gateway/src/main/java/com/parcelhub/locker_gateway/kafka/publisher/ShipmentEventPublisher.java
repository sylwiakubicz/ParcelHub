package com.parcelhub.locker_gateway.kafka.publisher;

import com.parcelhub.locker_gateway.kafka.config.KafkaProperties;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ShipmentEventPublisher {
    private final KafkaProperties kafkaProperties;
    private final KafkaTemplate<String, SpecificRecord> kafkaTemplate;
    private Logger logger = LoggerFactory.getLogger(ShipmentEventPublisher.class);

    public ShipmentEventPublisher(KafkaProperties kafkaProperties, KafkaTemplate<String, SpecificRecord> kafkaTemplate) {
        this.kafkaProperties = kafkaProperties;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String key, SpecificRecord msg) {
        CompletableFuture<SendResult<String, SpecificRecord>> future =
                kafkaTemplate.send(kafkaProperties.getShipmentEvents(), key, msg);
        future.whenComplete((r, e) -> {
            if (e == null) {
                logger.info("Message sent successfully {}", key);
            }
            else {
                logger.error("Error while sending message {}", key, e);
            }
        });
    }
}
