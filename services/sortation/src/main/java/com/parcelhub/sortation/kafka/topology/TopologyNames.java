package com.parcelhub.sortation.kafka.topology;

public class TopologyNames {
    public static final String SHIPMENT_ROUTE_STORE = "shipment-route-store";

    public static final String SHIPMENT_BY_ID_REPARTITION = "shipment-by-id";

    public static final String ARRIVED_AT_HUB_SOURCE = "arrived-at-hub-source";
    public static final String ROUTE_FROM_SHIPMENT_CREATED = "route-from-shipment-created";
    public static final String KEY_BY_SHIPMENT_ID_FROM_CREATED = "key-by-shipment-id-from-created";
    public static final String KEY_BY_SHIPMENT_ID_FROM_ARRIVED = "key-by-shipment-id-from-arrived";
    public static final String MISSING_ROUTE_META = "missing-route-meta";
    public static final String READY_FOR_DECISION = "ready-for-decision";

    private TopologyNames() {}
}
