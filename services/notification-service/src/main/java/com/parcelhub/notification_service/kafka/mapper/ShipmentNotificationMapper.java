package com.parcelhub.notification_service.kafka.mapper;

import com.parcelhub.notification.NotificationRequest;
import com.parcelhub.notification_service.kafka.dto.ShipmentNotificationState;
import com.parcelhub.shipment.ReadyForPickup;
import com.parcelhub.shipment.ShipmentCreated;

public class ShipmentNotificationMapper {

    public static ShipmentNotificationState from(ShipmentCreated shipmentCreated) {
        return new ShipmentNotificationState(
                shipmentCreated.getShipmentId(),
                shipmentCreated.getRecipient().getPhone(),
                shipmentCreated.getRecipient().getName(),
                shipmentCreated.getDestinationLockerId()
        );
    }

    public static NotificationRequest createNotificationRequest(
            ShipmentNotificationState shipmentNotificationState, ReadyForPickup readyForPickup) {
        if (shipmentNotificationState == null || shipmentNotificationState.recipientPhone() == null) return null;
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setChannel("SMS");
        notificationRequest.setBusinessKey("READY_FOR_PICKUP:" + readyForPickup.getShipmentId());
        notificationRequest.setTs(readyForPickup.getTs());
        notificationRequest.setTemplateId("ready_for_pickup_pl");
        notificationRequest.setRecipient(shipmentNotificationState.recipientPhone());
        notificationRequest.setEventId(readyForPickup.getEventId());
        notificationRequest.setVariables(createPayload(readyForPickup));
        return notificationRequest;
    }

    private static payload.shipmentData createPayload(ReadyForPickup readyForPickup) {
        payload.shipmentData payload = new payload.shipmentData();
        payload.setShipmentId(readyForPickup.getShipmentId());
        payload.setLockerId(readyForPickup.getLockerId());
        payload.setPickupCode(readyForPickup.getPickupCode());
        return payload;
    }
}
