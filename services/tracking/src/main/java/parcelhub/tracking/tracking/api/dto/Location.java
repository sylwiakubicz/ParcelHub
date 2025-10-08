package parcelhub.tracking.tracking.api.dto;

import parcelhub.tracking.tracking.api.enums.LocationType;

public class Location {
    private LocationType type;
    private String id;

    public Location() {
    }

    public LocationType getType() {
        return type;
    }

    public void setType(LocationType type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
