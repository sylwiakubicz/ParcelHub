package com.parcelhub.sortation.kafka.dto;

import com.parcelhub.shipment.ArrivedAtHub;

public record ArrivedAtHubWithRoute(ArrivedAtHub arrivedAtHub, ShipmentRoute route) {}
