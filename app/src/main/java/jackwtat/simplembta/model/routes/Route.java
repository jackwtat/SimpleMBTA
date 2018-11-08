package jackwtat.simplembta.model.routes;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jackwtat.simplembta.map.markers.StopMarkerFactory;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.model.Shape;
import jackwtat.simplembta.model.Stop;

public class Route implements Comparable<Route>, Serializable {
    public static final int LIGHT_RAIL = 0;
    public static final int HEAVY_RAIL = 1;
    public static final int COMMUTER_RAIL = 2;
    public static final int BUS = 3;
    public static final int FERRY = 4;
    public static final int UNKNOWN_MODE = -1;

    private String id;
    private int mode = UNKNOWN_MODE;
    private int sortOrder = -1;
    private String shortName = "null";
    private String longName = "null";
    private String primaryColor = "#FFFFFF";
    private String accentColor = "#3191E1";
    private String textColor = "#000000";
    private StopMarkerFactory markerFactory = new StopMarkerFactory();
    private Shape[] shapes = {};

    private ArrayList<ServiceAlert> serviceAlerts = new ArrayList<>();

    private String[] directionNames = {"Outbound", "Inbound"};
    private Stop[] nearestStops = new Stop[2];
    private ArrayList<ArrayList<Prediction>> predictions = new ArrayList<>();

    public Route(String id) {
        this.id = id;

        for (int i = 0; i < 2; i++) {
            predictions.add(new ArrayList<Prediction>());
        }
    }

    public String getId() {
        return id;
    }

    public int getMode() {
        return mode;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public String[] getDirectionNames() {
        return directionNames;
    }

    public String getDirectionName(int direction) {
        if (direction == 0 || direction == 1) {
            return directionNames[direction];
        } else {
            return "INVALID_DIRECTION";
        }
    }

    public MarkerOptions getStopMarkerOptions() {
        return markerFactory.createMarkerOptions();
    }

    public BitmapDescriptor getStopMarkerIcon() {
        return markerFactory.getIcon();
    }

    public Stop getNearestStop(int direction) {
        if (direction == 0 || direction == 1) {
            return nearestStops[direction];
        } else {
            return null;
        }
    }

    public ArrayList<Prediction> getPredictions(int direction) {
        if (direction == 0 || direction == 1) {
            return predictions.get(direction);
        } else {
            return null;
        }
    }

    public ArrayList<ServiceAlert> getServiceAlerts() {
        return serviceAlerts;
    }

    public Shape[] getShapes() {
        return shapes;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setShortName(String shortRouteName) {
        this.shortName = shortRouteName;
    }

    public void setLongName(String longRouteName) {
        this.longName = longRouteName;
    }

    public void setPrimaryColor(String primaryColor) {
        if (primaryColor.startsWith("#")) {
            this.primaryColor = primaryColor;
        } else {
            this.primaryColor = "#" + primaryColor;
        }
    }

    public void setAccentColor(String accentColor) {
        if (accentColor.startsWith("#")) {
            this.accentColor = accentColor;
        } else {
            this.accentColor = "#" + accentColor;
        }
    }

    public void setTextColor(String textColor) {
        if (textColor.startsWith("#")) {
            this.textColor = textColor;
        } else {
            this.textColor = "#" + textColor;
        }
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setDirectionNames(String[] directionNames) {
        for (int i = 0; i < 2 && i < directionNames.length; i++) {
            this.directionNames[i] = directionNames[i];
        }
    }

    public void setDirectionName(int direction, String name) {
        if (direction == 0 || direction == 1) {
            directionNames[direction] = name;
        }
    }

    public void setShapes(Shape[] shapes){
        this.shapes = shapes;
    }

    public void setStopMarkerFactory(StopMarkerFactory factory) {
        this.markerFactory = factory;
    }

    public void setNearestStop(int direction, Stop stop, boolean clearPredictions) {
        if (direction == 0 || direction == 1) {
            if (clearPredictions) {
                predictions.get(direction).clear();
            }
            nearestStops[direction] = stop;
        }
    }

    public void addPrediction(Prediction prediction) {
        int direction = prediction.getDirection();

        if (direction == 0 || direction == 1) {
            predictions.get(direction).add(prediction);
        }
    }

    public void addAllPredictions(List<Prediction> predictions) {
        for (Prediction p : predictions) {
            addPrediction(p);
        }
    }

    public void clearPredictions(int direction) {
        if (direction == 0 || direction == 1) {
            predictions.get(direction).clear();
        }
    }

    public boolean hasPredictions(int direction) {
        if (direction == 0 || direction == 1) {
            return predictions.get(direction).size() > 0;
        } else {
            return false;
        }
    }

    public boolean hasPickUps(int direction) {
        if (direction == 0 || direction == 1) {
            for (Prediction p : predictions.get(direction)) {
                if (p.willPickUpPassengers()) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    public boolean hasNearbyStops() {
        return nearestStops[0] != null || nearestStops[1] != null;
    }

    public void addServiceAlert(ServiceAlert serviceAlert) {
        if (!serviceAlerts.contains(serviceAlert)) {
            serviceAlerts.add(serviceAlert);
        }
    }

    public void addAllServiceAlerts(ServiceAlert[] serviceAlerts) {
        this.serviceAlerts.addAll(Arrays.asList(serviceAlerts));
    }

    public boolean hasUrgentServiceAlerts() {
        for (ServiceAlert serviceAlert : serviceAlerts) {
            if (serviceAlert.isActive() &&
                    (serviceAlert.getLifecycle() == ServiceAlert.Lifecycle.NEW ||
                            serviceAlert.getLifecycle() == ServiceAlert.Lifecycle.UNKNOWN)) {
                return true;
            }
        }

        return false;
    }

    public void clearServiceAlerts() {
        serviceAlerts.clear();
    }

    @Override
    public int compareTo(@NonNull Route otherRoute) {
        if (this.sortOrder != otherRoute.sortOrder) {
            return this.sortOrder - otherRoute.sortOrder;
        } else {
            return this.longName.compareTo(otherRoute.longName);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Route) {
            Route otherRoute = (Route) obj;
            return id.equals(otherRoute.id);
        } else {
            return false;
        }
    }

    public boolean idEquals(String id) {
        return this.id.equals(id);
    }
}
