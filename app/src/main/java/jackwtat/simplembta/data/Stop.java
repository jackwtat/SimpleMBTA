package jackwtat.simplembta.data;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by jackw on 8/26/2017.
 */

public class Stop implements Comparable<Stop> {
    private static final String TAG = "Stop";

    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private double distance;
    private ArrayList<Route> routeList = new ArrayList<>();
    private ArrayList<Trip> tripList = new ArrayList<>();

    public Stop(String id) {
        this.id = id;
        this.name = "";
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.distance = 0.0;
    }

    public Stop(String id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = 0.0;
    }

    public Stop(String id, String name, double latitude, double longitude, double distance) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
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

    public ArrayList<Route> getRoutes() { return routeList; }

    public ArrayList<Trip> getTrips() { return tripList; }

    public void setName(String name) {
        this.name = name;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void addTrip(Trip trip) {
        tripList.add(trip);

        if (!routeList.contains(trip.getRoute())) {
            routeList.add(trip.getRoute());
        }
    }

    public void addTrips(ArrayList<Trip> tripList) {
        for (int i = 0; i < tripList.size(); i++) {
            Trip trip = tripList.get(i);

            this.tripList.add(trip);

            if (!routeList.contains(trip.getRoute())) {
                routeList.add(trip.getRoute());
            }
        }
    }

    //    Create an array for each route's next tripList in each direction
    //    Returns Trip[x][y][z]
    //        x = route
    //        y = direction, i.e. inbound/outbound
    //        z = next tripList
    public Trip[][][] getSortedTripArray(int perDirectionLimit) {
        Trip[][][] tripArray = new Trip[routeList.size()][Route.Direction.COUNT][perDirectionLimit];

        // Sort the routes
        Collections.sort(routeList);

        // Populate the array of tripList
        // Loop through all tripList at this stop
        for (int i = 0; i < tripList.size(); i++) {
            Trip trip = tripList.get(i);

            // Get direction of the trip
            int k = trip.getDirection();

            // Find the corresponding position of route in list
            // and populate into tripList array
            for (int j = 0; j < routeList.size(); j++) {
                if (trip.getRouteId().equals(routeList.get(j).getId())) {
                    /*
                        Correct position of route & direction found
                        Order of insertion of trip:
                          1. If current slot is empty
                                Insert new trip into current slot
                          2. If trip arrival time is less than slot's arrival time
                                Shift next tripList up by one
                                Insert new trip into current slot
                    */
                    for (int m = 0; m < tripArray[j][k].length; m++) {
                        if (tripArray[j][k][m] == null) {
                            tripArray[j][k][m] = trip;
                            m = tripArray[j][k].length;
                        } else if (trip.getArrivalTime() < tripArray[j][k][m].getArrivalTime()) {
                            // Shift all tripList right
                            for (int n = tripArray[j][k].length - 1; n > m; n--) {
                                tripArray[j][k][n] = tripArray[j][k][n - 1];
                            }
                            tripArray[j][k][m] = trip;
                            m = tripArray[j][k].length;
                        }
                    }

                    // Terminate j-loop to move to next trip
                    break;
                }
            }
        }

        return tripArray;
    }

    @Override
    public int compareTo(@NonNull Stop anotherStop) {
        return Double.compare(this.distance, anotherStop.distance);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Stop) {
            Stop anotherStop = (Stop) obj;
            return this.id.equals(anotherStop.getId());
        }

        return false;
    }
}
