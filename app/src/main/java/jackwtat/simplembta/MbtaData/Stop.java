package jackwtat.simplembta.MbtaData;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jackw on 8/26/2017.
 */

public class Stop implements Comparable{
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private List<Route> routes = new ArrayList<>();
    private List<Prediction> predictions = new ArrayList<>();

    public Stop(String id, String name){
        this.id = id;
        this.name = name;
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    public Stop(String id, String name, float latitude, float longitude) {
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

    public List<Route> getRoutes() { return routes; }

    public List<Prediction> getPredictions() {
        return predictions;
    }

    public void clearPredictions(){
        predictions.clear();
    }

    public void addRoute(Route route) { routes.add(route); }

    public void addRoutes(List<Route> routes) { this.routes.addAll(routes); }

    public void addPrediction(Prediction prediction){
        predictions.add(prediction);
    }

    public void addPredictions(List<Prediction> predictions){ this.predictions.addAll(predictions); }

    @Override
    public int compareTo(@NonNull Object o) {
        // TODO: Implement compareTo
        return 0;
    }
}
