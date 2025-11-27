package com.parcelhub.locker_gateway.exception;

public class InvalidLockerId extends RuntimeException {
    public InvalidLockerId(String lockerId) {
        super("Invalid locker id: " + lockerId);
    }
}
