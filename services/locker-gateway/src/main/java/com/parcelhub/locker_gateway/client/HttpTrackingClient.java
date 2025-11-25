package com.parcelhub.locker_gateway.client;

import com.parcelhub.locker_gateway.dto.ShipmentInfo;
import com.parcelhub.locker_gateway.dto.TrackingClientResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class HttpTrackingClient implements TrackingClient {

    private final RestClient restClient;
    private final String baseUrl;

    public HttpTrackingClient(
            RestClient.Builder builder,
            @Value("${tracking-service.base-url}") String baseUrl
    ) {
        this.restClient = builder.build();
        this.baseUrl = baseUrl;
    }

    @Override
    public Optional<ShipmentInfo> getShipmentInfo(String shipmentId) {
        try {
            TrackingClientResponse response = restClient.get()
                    .uri(baseUrl + "/{shipmentId}/tracking", shipmentId)
                    .retrieve()
                    .onStatus(HttpStatus.NOT_FOUND::equals, (req, res) -> {})
                    .body(TrackingClientResponse.class);
            if (response == null) {
                return Optional.empty();
            }

            ShipmentInfo info = new ShipmentInfo(shipmentId, response.getStatus(), response.getDestinationLockerId());
            return Optional.of(info);
        }
        catch (Exception e) {
            throw new IllegalStateException("Tracking service unavailable", e);
        }
    }
}
