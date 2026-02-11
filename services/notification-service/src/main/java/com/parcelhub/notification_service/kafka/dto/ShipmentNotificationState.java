package com.parcelhub.notification_service.kafka.dto;

public record ShipmentNotificationState(String shipmentId, String recipientPhone, String recipientEmail,
                                        String recipientName, String destinationLockerId) {
}
