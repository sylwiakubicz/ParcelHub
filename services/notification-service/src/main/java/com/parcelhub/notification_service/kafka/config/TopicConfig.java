package com.parcelhub.notification_service.kafka.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class TopicConfig {

    @Value("${topics.shipment-events}")
    private String shipmentEvents;

    @Value("${topics.notification-requests}")
    private String notificationRequests;

    @Value("${topics.notification-requests.retry.5s}")
    private String notificationRequestRetry5s;

    @Value("${topics.notification-requests.retry.1m}")
    private String notificationRequestRetry1m;

    @Value("${topics.notification-requests.retry.10m}")
    private String notificationRequestRetry10m;

    @Value("${topics.notification-requests.dlq}")
    private String notificationRequestDlq;

}
