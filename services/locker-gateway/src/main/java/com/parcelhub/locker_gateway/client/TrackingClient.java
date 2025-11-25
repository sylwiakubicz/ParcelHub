package com.parcelhub.locker_gateway.client;

import com.parcelhub.locker_gateway.dto.ShipmentInfo;

import java.util.Optional;

public interface TrackingClient {
    Optional<ShipmentInfo> getShipmentInfo(String shipmentId);
}
