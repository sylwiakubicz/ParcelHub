package parcelhub.tracking.tracking.api.enums;

public enum ShipmentStatus {
    CREATED,
    IN_TRANSIT,
    AT_HUB,
    DELIVERED_TO_LOCKER,
    READY_FOR_PICKUP,
    PICKED_UP,

    RETURN_INITIATED,
    RETURN_IN_TRANSIT,
    RETURN_DELIVERED_TO_LOCKER,
    RETURN_READY_FOR_PICKUP,
    RETURN_COMPLETED,

    EXPIRED_AT_LOCKER,
    CANCELLED,
    LOST
}
