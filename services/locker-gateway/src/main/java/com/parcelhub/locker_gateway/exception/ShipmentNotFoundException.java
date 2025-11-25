package com.parcelhub.locker_gateway.exception;

public class ShipmentNotFoundException extends RuntimeException {
    public ShipmentNotFoundException(String shipmentId) {
        super("Shipment " + shipmentId + " not found");
    }
}
