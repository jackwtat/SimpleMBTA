package jackwtat.simplembta;

import java.util.ArrayList;

/**
 * Created by jackw on 8/26/2017.
 */

public class Stop {
    private String id;
    private String name;
    private float latitude;
    private float longitude;
    private ArrayList<Trip> trips;

    public Stop(String id, String name, float latitude, float longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean hasId(String id) {
        return id.equals(this.id);
    }

    public String getName() {
        return name;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public ArrayList<Trip> getTrips(){
        return trips;
    }

    public void addTrip(Trip trip){
        trips.add(trip);
    }

}
