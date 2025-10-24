package parcelhub.tracking.kafka.topology;

public final class TopologyNames {
    public static final String SHIPMENT_EVENTS_STREAM = "shipment-events-stream";
    public static final String TRACKING_TABLE_STORE = "shipment-tracking-store";
    public static final String TRACKING_DELTAS_STREAM = "tracking-deltas-stream";
    public static final String STATUS_INDEX_STORE = "status-index-store";

    private TopologyNames() {}
}
