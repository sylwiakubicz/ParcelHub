package com.parcelhub.locker_gateway.kafka.config;

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
}
