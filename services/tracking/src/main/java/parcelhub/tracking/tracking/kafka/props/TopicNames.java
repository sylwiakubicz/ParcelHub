package parcelhub.tracking.tracking.kafka.props;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicNames {
    @Value("${topics.shipment-events}")
    private String shipmentEvents;

    @Value("${topics.shipment-tracking}")
    private String shipmentTracking;

    @Value("${topics.tracking-updates}")
    private String trackingUpdates;

    @Value("${topics.internal.changelog}")
    private String internalChangelog;

    public String getShipmentEvents() {
        return shipmentEvents;
    }

    public String getShipmentTracking() {
        return shipmentTracking;
    }

    public String getTrackingUpdates() {
        return trackingUpdates;
    }

    public String getInternalChangelog() {
        return internalChangelog;
    }
}
