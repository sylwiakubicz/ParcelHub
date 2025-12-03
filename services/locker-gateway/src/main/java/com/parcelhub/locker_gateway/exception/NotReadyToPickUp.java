package com.parcelhub.locker_gateway.exception;

public class NotReadyToPickUp extends RuntimeException {
    public NotReadyToPickUp(String message) {
        super("Shipment not ready to pick up");
    }
}
