package com.parcelhub.sortation.kafka.mapper;

import com.parcelhub.shipment.ShipmentCreated;
import com.parcelhub.sortation.kafka.dto.ShipmentRoute;

public class ShipmentRouteMapper {

    public ShipmentRoute from(ShipmentCreated sc) {
        ShipmentRoute route = new ShipmentRoute();
        route.setShipmentId(sc.getShipmentId());
        route.setDestinationLockerId(sc.getDestinationLockerId());
        return route;
    }
}
