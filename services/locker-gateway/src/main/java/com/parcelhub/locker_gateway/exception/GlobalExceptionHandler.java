package com.parcelhub.locker_gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShipmentNotFoundException.class)
    public ResponseEntity<?> handleShipmentNotFoundException(ShipmentNotFoundException e) {
        ApiError error = new ApiError(
                "SHIPMENT_NOT_FOUND",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException e) {
        ApiError error = new ApiError(
                "TRACKING_SERVICE_UNAVAILABLE",
                "Tracking service is unavailable"
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(InvalidLockerId.class)
    public ResponseEntity<?> handleInvalidLockerId(InvalidLockerId e) {
        ApiError error = new ApiError(
                "INVALID_LOCKER_ID",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(NotReadyToPickUp.class)
    public ResponseEntity<?> handleNotReadyToPickUp(NotReadyToPickUp e) {
        ApiError error = new ApiError(
                "NOT_READY_FOR_PICKUP",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidPickupCodeException.class)
    public ResponseEntity<?> handleInvalidPickupCodeException(InvalidPickupCodeException e) {
        ApiError error = new ApiError(
                "INVALID_PICKUP_CODE",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ReadyToPickupProcessingException.class)
    public ResponseEntity<?> handleReadyToPickupFailed(ReadyToPickupProcessingException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(
                        "READY_TO_PICKUP_FAILED",
                        e.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        ApiError error = new ApiError("INTERNAL_ERROR", "Unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
