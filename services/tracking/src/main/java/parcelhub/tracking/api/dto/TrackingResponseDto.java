package parcelhub.tracking.api.dto;

import com.parcelhub.tracking.ShipmentStatus;
import com.parcelhub.tracking.ShipmentTrackingState;

public class TrackingResponseDto {
    private ShipmentStatus status;
    private long lastUpdated;
    private LastLocationDto lastLocation;
    private String destinationLockerId;
    private int version;

    public static TrackingResponseDto from(ShipmentTrackingState s) {
        TrackingResponseDto dto = new TrackingResponseDto();
        dto.status = s.getStatus();
        dto.lastUpdated = s.getLastUpdate() != null ? s.getLastUpdate().toEpochMilli() : 0L;
        dto.lastLocation = LastLocationDto.from(s.getLastLocation());
        dto.destinationLockerId = s.getDestinationLockerId();
        dto.version = s.getVersion();
        return dto;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public LastLocationDto getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(LastLocationDto lastLocation) {
        this.lastLocation = lastLocation;
    }

    public String getDestinationLockerId() {
        return destinationLockerId;
    }

    public void setDestinationLockerId(String destinationLockerId) {
        this.destinationLockerId = destinationLockerId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
