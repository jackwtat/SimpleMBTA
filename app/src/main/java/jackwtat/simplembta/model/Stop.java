package jackwtat.simplembta.model;

import android.support.annotation.NonNull;

public class Stop implements Comparable<Stop> {
    private String id;
    private String name;
    private String[] childIds;
    private double latitude;
    private double longitude;
    private double distance;

    public Stop(String id) {
        this.id = id;
        name = "null";
        childIds = new String[0];
        latitude = 0.0;
        longitude = 0.0;
        distance = 0.0;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getDistance() {
        return distance;
    }

    public String[] getChildIds() {
        return childIds;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChildIds(String[] childIds) {
        this.childIds = childIds;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isParentOf(String id) {
        for (String childId : childIds) {
            if (childId.equals(id)) {
                return true;
            }
        }

        return false;
    }

    public void setDistance(double originLat, double originLon) {
        final double MILES_PER_LAT = 69;
        final double MILES_PER_LON = 69.172;

        distance = Math.sqrt(Math.pow((latitude - originLat) * MILES_PER_LAT, 2) +
                Math.pow((longitude - originLon) * MILES_PER_LON, 2));
    }

    @Override
    public int compareTo(@NonNull Stop otherStop) {
        if (this.distance == otherStop.getDistance()) {
            return this.id.compareTo(otherStop.getId());
        } else {
            return Double.compare(this.distance, otherStop.getDistance());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Stop) {
            Stop otherStop = (Stop) obj;
            return id.equals(otherStop.getId());
        } else {
            return false;
        }
    }
}
