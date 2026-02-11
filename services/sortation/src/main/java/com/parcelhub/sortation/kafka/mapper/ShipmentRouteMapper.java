package com.parcelhub.sortation.kafka.mapper;

import com.parcelhub.shipment.ShipmentCreated;
import com.parcelhub.shipment.SortedToDestLocker;
import com.parcelhub.shipment.SortedToNextHub;
import com.parcelhub.sortation.kafka.dto.ArrivedAtHubWithRoute;
import com.parcelhub.sortation.kafka.dto.ShipmentRoute;

import java.util.UUID;

public class ShipmentRouteMapper {

    public ShipmentRoute from(ShipmentCreated sc) {
        ShipmentRoute route = new ShipmentRoute();
        route.setShipmentId(sc.getShipmentId());
        route.setDestinationLockerId(sc.getDestinationLockerId());
        return route;
    }

    public SortedToDestLocker sortedToLockerFrom(ArrivedAtHubWithRoute v) {
        SortedToDestLocker locker = new SortedToDestLocker();
        locker.setShipmentId(v.arrivedAtHub().getShipmentId());
        locker.setLockerId(v.route().getDestinationLockerId());
        locker.setTs(v.arrivedAtHub().getTs());
        locker.setEventId(UUID.randomUUID());
        return locker;
    }

    public SortedToNextHub sortedTOnNextHubFrom(ArrivedAtHubWithRoute v) {
        SortedToNextHub nextHub = new SortedToNextHub();
        nextHub.setEventId(UUID.randomUUID());
        nextHub.setTs(v.arrivedAtHub().getTs());
        nextHub.setShipmentId(v.arrivedAtHub().getShipmentId());
        nextHub.setFromHubId(v.arrivedAtHub().getHubId());
        nextHub.setToHubId(v.route().getDestinationLockerId().substring(0, 3).toUpperCase());
        return nextHub;
    }
}
