package jackwtat.simplembta.mbta.structure;

import android.support.annotation.NonNull;

/**
 * Created by jackw on 12/11/2017.
 */

public class Trip implements Comparable<Trip> {
    private String id = "";
    private int direction = 0;
    private String destination = "";
    private String name = "";

    public Trip(String id, int direction, String destination, String name) {
        this.id = id;
        this.direction = direction;
        this.destination = destination;
        this.name = name;
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

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(@NonNull Trip trip) {
        return trip.getDirection() - getDirection();
    }
}