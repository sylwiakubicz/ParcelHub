package com.parcelhub.locker_gateway.exception;

public class LockerNotAvailableException extends RuntimeException {
    public LockerNotAvailableException(String message) {
        super(message);
    }
}
