package jackwtat.simplembta.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jackw on 12/12/2017.
 */

public class Stop implements Comparable<Stop> {

    private String id = "";
    private String name = "";
    private String parentStopId = "";
    private boolean hasParentStop = false;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private double distance = 0.0;
    private ArrayList<Prediction> predictions = new ArrayList<>();

    public Stop(String id, String name, String parentStopId, double latitude, double longitude,
                double distance) {
        this.id = id;
        this.name = name;
        this.parentStopId = parentStopId;
        this.hasParentStop = true;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
        this.predictions = new ArrayList<>();
    }

    public Stop(String id, String name, String parentStopId, double stopLatitude,
                double stopLongitude, double userLatitude, double userLongitude) {
        this.id = id;
        this.name = name;
        this.parentStopId = parentStopId;
        this.hasParentStop = true;
        this.latitude = stopLatitude;
        this.longitude = stopLongitude;
        this.distance = calculateDistance(userLatitude, userLongitude);
    }

    public Stop(String id, String name, double latitude, double longitude, double distance) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }

    public Stop(String id, String name, double latitude, double longitude,
                double userLatitude, double userLongitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = calculateDistance(userLatitude, userLongitude);
    }

    public Stop(String id, String name, String parentStopId, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.parentStopId = parentStopId;
        this.hasParentStop = true;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Stop(String id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Stop(String id, String name, String parentStopId) {
        this.id = id;
        this.name = name;
        this.parentStopId = parentStopId;
        this.hasParentStop = true;
    }

    public Stop(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean hasParentStop() {
        return hasParentStop;
    }

    public String getParentStopId() {
        return parentStopId;
    }

    public void setParentStopId(String parentStopId) {
        this.parentStopId = parentStopId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double userLatitude, double userLongitude) {
        this.distance = calculateDistance(userLatitude, userLongitude);
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public ArrayList<Prediction> getPredictions() {
        return predictions;
    }


    public void addPrediction(Prediction prediction) {
        predictions.add(prediction);
    }

    public void addPredictions(List<Prediction> predictions) {
        this.predictions.addAll(predictions);
    }

    private double calculateDistance(double originLat, double originLon) {
        final double MILES_PER_LAT = 69;
        final double MILES_PER_LON = 69.172;

        return Math.sqrt(Math.pow((this.latitude - originLat) * MILES_PER_LAT, 2) +
                Math.pow((this.longitude - originLon) * MILES_PER_LON, 2));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Stop) {
            Stop stop = (Stop) obj;
            return this.id.equals(stop.getId());
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(@NonNull Stop stop) {
        return Double.compare(this.distance, stop.getDistance());
    }
}
