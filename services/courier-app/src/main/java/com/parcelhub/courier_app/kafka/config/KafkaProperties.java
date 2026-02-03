package com.parcelhub.courier_app.kafka.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaProperties {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.apicurio.registry.url}")
    private String apicurioUrl;

    @Value("${topics.shipment-events}")
    private String shipmentEvents;

    @Value("${topics.scan-events.locker}")
    private String scanEventsLocker;

    @Value("${topics.scan-events.hub}")
    private String scanEventsHub;

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getApicurioUrl() {
        return apicurioUrl;
    }

    public void setApicurioUrl(String apicurioUrl) {
        this.apicurioUrl = apicurioUrl;
    }

    public String getShipmentEvents() {
        return shipmentEvents;
    }

    public void setShipmentEvents(String shipmentEvents) {
        this.shipmentEvents = shipmentEvents;
    }

    public String getScanEventsLocker() {
        return scanEventsLocker;
    }

    public void setScanEventsLocker(String scanEventsLocker) {
        this.scanEventsLocker = scanEventsLocker;
    }

    public String getScanEventsHub() {
        return scanEventsHub;
    }

    public void setScanEventsHub(String scanEventsHub) {
        this.scanEventsHub = scanEventsHub;
    }
}