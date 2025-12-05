package com.parcelhub.locker_gateway.exception;

public class InvalidPickupCodeException extends RuntimeException {
    public InvalidPickupCodeException(String message) {
        super(message);
    }
}
