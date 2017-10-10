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
    private List<Prediction> predictions = new ArrayList<>();

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

    public List<Prediction> getSortedPredictions() {
        return predictions;
    }

    public void clearPredictions() {
        predictions.clear();
    }

    public void addRoute(Route route) {
        routes.add(route);
    }

    public void addRoutes(List<Route> routes) {
        this.routes.addAll(routes);
    }

    public void addPrediction(Prediction prediction) {
        predictions.add(prediction);
    }

    public void addPredictions(List<Prediction> predictions) {
        this.predictions.addAll(predictions);
    }

    //    Create an array for each route's next predictions in each direction
    //    Returns Prediction[x][y][z]
    //        x = route
    //        y = direction, i.e. inbound/outbound
    //        z = next predictions
    public Prediction[][][] getSortedPredictions(int perDirectionLimit) {
        Prediction[][][] predArray = new Prediction[routes.size()][Route.DIRECTIONS.length][perDirectionLimit];

        // Populate the array of predictions
        // Loop through all predictions at this stop
        for (int i = 0; i < predictions.size(); i++) {
            Prediction prediction = predictions.get(i);

            // Get direction of the prediction
            int k = prediction.getDirection();

            // Find the corresponding position of route in list
            // and populate into predictions array
            for (int j = 0; j < routes.size(); j++) {
                if (prediction.getRouteId().equals(routes.get(j).getId())) {
                    /*
                        Correct position of route & direction found
                        Order of insertion of prediction:
                          1. If current slot is empty
                                Insert new prediction into current slot
                          2. If prediction arrival time is less than slot's arrival time
                                Shift next predictions up by one
                                Insert new prediction into current slot
                    */
                    for (int m = 0; m < predArray[j][k].length; m++) {
                        if (predArray[j][k][m] == null) {
                            predArray[j][k][m] = prediction;
                            m = predArray[j][k].length;
                        } else if (prediction.getArrivalTime() < predArray[j][k][m].getArrivalTime()) {
                            // Shift all predictions right
                            for (int n = predArray[j][k].length - 1; n > m; n--) {
                                predArray[j][k][n] = predArray[j][k][n - 1];
                            }
                            predArray[j][k][m] = prediction;
                            m = predArray[j][k].length;
                        }
                    }

                    // Terminate j-loop to move to next prediction
                    j = routes.size();
                }
            }
        }

        return predArray;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        // TODO: Implement compareTo
        return 0;
    }
}
