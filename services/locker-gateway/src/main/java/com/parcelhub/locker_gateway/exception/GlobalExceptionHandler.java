package com.parcelhub.locker_gateway.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShipmentNotFoundException.class)
    public ResponseEntity<?> handleShipmentNotFoundException(ShipmentNotFoundException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException e) {
        return ResponseEntity.status(503).body("Tracking service is unavailable");
    }

    @ExceptionHandler(InvalidLockerId.class)
    public ResponseEntity<?> handleInvalidLockerId(InvalidLockerId e) {
        return ResponseEntity.status(503).body(e.getMessage());
    }
}
