package com.parcelhub.locker_gateway.exception;

import org.springframework.http.HttpStatus;
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

    @ExceptionHandler(NotReadyToPickUp.class)
    public ResponseEntity<?> handleNotReadyToPickUp(NotReadyToPickUp e) {
        return ResponseEntity.status(503).body(e.getMessage());
    }

    @ExceptionHandler(InvalidPickupCodeException.class)
    public ResponseEntity<?> handleInvalidPickupCodeException(InvalidPickupCodeException e) {
        return ResponseEntity.status(503).body(e.getMessage());
    }

    @ExceptionHandler(ReadyToPickupProcessingException.class)
    public ResponseEntity<?> handleReadyToPickupFailed(ReadyToPickupProcessingException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(
                        "READY_TO_PICKUP_FAILED",
                        "Nie udało się przygotować przesyłki do odbioru. Spróbuj ponownie później."
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        ApiError error = new ApiError("INTERNAL_ERROR", "Unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
