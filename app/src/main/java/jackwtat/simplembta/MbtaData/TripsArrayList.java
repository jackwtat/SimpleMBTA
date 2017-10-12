package jackwtat.simplembta.MbtaData;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by jackw on 10/11/2017.
 */

public class TripsArrayList extends ArrayList<Trip> {
    public ArrayList<String> routes = new ArrayList<>();

    public void addTrip(Trip trip) {
        String routeId = trip.getRouteId();

        if (!routes.contains(routeId)) {
            System.out.println(routeId);
            routes.add(routeId);
        }
        super.add(trip);
    }
}
