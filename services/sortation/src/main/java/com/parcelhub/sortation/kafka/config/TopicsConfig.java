package com.parcelhub.sortation.kafka.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicsConfig {

    @Value("${topics.shipment-events}")
    private String shipmentEvents;

    @Value("${topics.scan-events.hub}")
    private String scanEventsHub;

    public String getShipmentEvents() {
        return shipmentEvents;
    }

    public String getScanEventsHub() {
        return scanEventsHub;
    }
}
