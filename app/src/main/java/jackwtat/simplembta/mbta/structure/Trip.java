package jackwtat.simplembta.mbta.structure;

import android.support.annotation.NonNull;

/**
 * Created by jackw on 12/11/2017.
 */

public class Trip implements Comparable<Trip> {
    private String id = "";
    private int direction = 0;
    private String destination = "";

    public Trip(String id, int direction, String destination) {
        this.id = id;
        this.direction = direction;
        this.destination = destination;
    }

    public String getId() {
        return id;
    }

    public int getDirection() {
        return direction;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public int compareTo(@NonNull Trip trip) {
        if (this.direction > trip.getDirection()) {
            return -1;
        } else if (this.direction < trip.getDirection()) {
            return 1;
        } else {
            return 0;
        }
    }
}