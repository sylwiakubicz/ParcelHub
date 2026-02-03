package com.parcelhub.courier_app.service;

import com.parcelhub.courier_app.dto.ArrivedAtHubRequestDto;
import com.parcelhub.courier_app.dto.CollectRequestDto;
import com.parcelhub.courier_app.kafka.publisher.CourierKafkaPublisher;
import com.parcelhub.shipment.ArrivedAtHub;
import com.parcelhub.shipment.CollectedFromLocker;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class CourierService {
    private final String source = "courier-app";
    private final CourierKafkaPublisher courierKafkaPublisher;

    public CourierService(CourierKafkaPublisher courierKafkaPublisher) {
        this.courierKafkaPublisher = courierKafkaPublisher;
    }

    public void collect(String courierId, CollectRequestDto collectRequestDto, String traceId, String correlationId) {

        CollectedFromLocker collectedFromLocker = new CollectedFromLocker();
        collectedFromLocker.setCourierId(courierId);
        collectedFromLocker.setShipmentId(collectRequestDto.getShipmentId());
        collectedFromLocker.setLockerId(collectRequestDto.getLockerId());
        collectedFromLocker.setTs(Instant.now());
        collectedFromLocker.setEventId(UUID.randomUUID());

        courierKafkaPublisher.sendShipmentEvent(
                collectRequestDto.getShipmentId().toString(),
                collectedFromLocker,
                Map.of(
                        "traceId", traceId,
                        "correlationId", correlationId,
                        "source", source,
                        "event_type", "collected_from_locker"
                )
                );
        courierKafkaPublisher.sendScanEventLocker(
                collectRequestDto.getShipmentId().toString(),
                collectedFromLocker,
                Map.of(
                        "traceId", traceId,
                        "correlationId", correlationId,
                        "source", source,
                        "event_type", "collected_from_locker"
                )
                );
    }

    public void arrivedAtHub(String courierId, ArrivedAtHubRequestDto arrivedAtHubRequestDto,
                             String traceId, String correlationId) {

        ArrivedAtHub arrivedAtHub = new ArrivedAtHub();
        arrivedAtHub.setHubId(arrivedAtHubRequestDto.getHubId());
        arrivedAtHub.setShipmentId(arrivedAtHubRequestDto.getShipmentId());
        arrivedAtHub.setEventId(UUID.randomUUID());
        arrivedAtHub.setTs(Instant.now());

        courierKafkaPublisher.sendShipmentEvent(
                arrivedAtHubRequestDto.getShipmentId().toString(),
                arrivedAtHub,
                Map.of(
                        "traceId", traceId,
                        "correlationId", correlationId,
                        "source", source,
                        "event_type", "arrived_at_hub"
                )
                );
        courierKafkaPublisher.sendScanEventHub(
                arrivedAtHubRequestDto.getShipmentId().toString(),
                arrivedAtHub,
                Map.of(
                        "traceId", traceId,
                        "correlationId", correlationId,
                        "source", source,
                        "event_type", "arrived_at_hub"
                )
                );
    }
}
