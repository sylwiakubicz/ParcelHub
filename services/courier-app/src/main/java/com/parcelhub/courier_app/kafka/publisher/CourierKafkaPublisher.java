package com.parcelhub.courier_app.kafka.publisher;

import com.parcelhub.courier_app.kafka.config.KafkaProperties;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class CourierKafkaPublisher {

    private final KafkaProperties kafkaProperties;
    private final KafkaTemplate<String, SpecificRecord> kafkaTemplate;
    private Logger logger = LoggerFactory.getLogger(CourierKafkaPublisher.class);

    public CourierKafkaPublisher(KafkaProperties kafkaProperties, KafkaTemplate<String, SpecificRecord> kafkaTemplate) {
        this.kafkaProperties = kafkaProperties;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendShipmentEvent(String key, SpecificRecord msg, Map<String, String> headers) {
        sendMessage(kafkaProperties.getShipmentEvents(), key, msg, headers );
    }

    public void sendScanEvent(String key, SpecificRecord msg, Map<String, String> headers) {
        sendMessage(kafkaProperties.getScanEvents(), key, msg, headers );
    }

    private void sendMessage(String topic, String key, SpecificRecord msg, Map<String, String> headers) {

        List<Header> kafkaHeaders = headers.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> (Header) new RecordHeader(
                        e.getKey(),
                        e.getValue().getBytes(StandardCharsets.UTF_8)) {
                } )
                .toList();


        ProducerRecord<String, SpecificRecord> record =
                new ProducerRecord<>(topic, null, key, msg, kafkaHeaders);

        CompletableFuture<SendResult<String, SpecificRecord>> future =
                kafkaTemplate.send(record);
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

