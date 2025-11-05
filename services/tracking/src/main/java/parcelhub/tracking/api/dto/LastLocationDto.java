package parcelhub.tracking.api.dto;

import com.parcelhub.tracking.LastLocation;
import com.parcelhub.tracking.LocationType;

public class LastLocationDto {
    private LocationType type;
    private String id;

    public static LastLocationDto from(LastLocation src) {
        LastLocationDto dto = new LastLocationDto();
        dto.type = (src != null && src.getType() != null) ? src.getType() : LocationType.NONE;
        dto.id = (src != null && src.getId() != null) ? src.getId().toString() : null;
        return dto;
    }

    public LocationType getType() { return type; }
    public void setType(LocationType type) { this.type = type; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
