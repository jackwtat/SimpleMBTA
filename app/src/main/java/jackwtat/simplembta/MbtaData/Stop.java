package jackwtat.simplembta.MbtaData;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jackw on 8/26/2017.
 */

public class Stop implements Comparable {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private List<Route> routes = new ArrayList<>();
    private List<Trip> trips = new ArrayList<>();

    public Stop(String id, String name) {
        this.id = id;
        this.name = name;
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    public Stop(String id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public List<Route> getRoutes() {
        return routes;
    }

    public List<Trip> getSortedTrips() {
        return trips;
    }

    public void clearPredictions() {
        trips.clear();
    }

    public void addRoute(Route route) {
        routes.add(route);
    }

    public void addRoutes(List<Route> routes) {
        this.routes.addAll(routes);
    }

    public void addPrediction(Trip trip) {
        trips.add(trip);
    }

    public void addTrips(List<Trip> trips) {
        this.trips.addAll(trips);
    }

    //    Create an array for each route's next trips in each direction
    //    Returns Trip[x][y][z]
    //        x = route
    //        y = direction, i.e. inbound/outbound
    //        z = next trips
    public Trip[][][] getSortedTrips(int perDirectionLimit) {
        Trip[][][] tripArray = new Trip[routes.size()][Route.DIRECTIONS.length][perDirectionLimit];

        // Populate the array of trips
        // Loop through all trips at this stop
        for (int i = 0; i < trips.size(); i++) {
            Trip trip = trips.get(i);

            // Get direction of the trip
            int k = trip.getDirection();

            // Find the corresponding position of route in list
            // and populate into trips array
            for (int j = 0; j < routes.size(); j++) {
                if (trip.getRouteId().equals(routes.get(j).getId())) {
                    /*
                        Correct position of route & direction found
                        Order of insertion of trip:
                          1. If current slot is empty
                                Insert new trip into current slot
                          2. If trip arrival time is less than slot's arrival time
                                Shift next trips up by one
                                Insert new trip into current slot
                    */
                    for (int m = 0; m < tripArray[j][k].length; m++) {
                        if (tripArray[j][k][m] == null) {
                            tripArray[j][k][m] = trip;
                            m = tripArray[j][k].length;
                        } else if (trip.getArrivalTime() < tripArray[j][k][m].getArrivalTime()) {
                            // Shift all trips right
                            for (int n = tripArray[j][k].length - 1; n > m; n--) {
                                tripArray[j][k][n] = tripArray[j][k][n - 1];
                            }
                            tripArray[j][k][m] = trip;
                            m = tripArray[j][k].length;
                        }
                    }

                    // Terminate j-loop to move to next trip
                    j = routes.size();
                }
            }
        }

        return tripArray;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        // TODO: Implement compareTo
        return 0;
    }
}
