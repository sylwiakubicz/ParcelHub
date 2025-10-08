package parcelhub.tracking.tracking.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import parcelhub.tracking.tracking.api.dto.ShipmentTrackingResponse;

@RestController
@RequestMapping("/shipments")
public class TrackingController {

    @GetMapping("/{shipmentId}/tracking")
    public ResponseEntity<ShipmentTrackingResponse> tracking(@PathVariable String shipmentId) {
        // tutaj bedzie wywolana metoda wyciagajaca aktualny status z KTable (jak juz ja napisze)
        // najpewniej wtedy też dojdzie mapowanie tego co zostanie zwrócone na ShipmentTrackingResponse
        ShipmentTrackingResponse response = new ShipmentTrackingResponse();
        return ResponseEntity.ok(response);
    }
}
