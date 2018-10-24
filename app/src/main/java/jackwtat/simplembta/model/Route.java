package jackwtat.simplembta.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jackwtat.simplembta.R;

public class Route implements Comparable<Route>, Serializable {
    public static final int OUTBOUND = 0;
    public static final int INBOUND = 1;
    public static final int WESTBOUND = 0;
    public static final int EASTBOUND = 1;
    public static final int SOUTHBOUND = 0;
    public static final int NORTHBOUND = 1;
    public static final int NULL_DIRECTION = 0;

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

    // Returns the language-specific short name of this route
    // Context is required to get the proper translation
    public String getShortDisplayName(Context context) {
        if (mode == HEAVY_RAIL) {
            if (id.equals("Red"))
                return context.getResources().getString(R.string.red_line_short_name);
            else if (id.equals("Orange"))
                return context.getResources().getString(R.string.orange_line_short_name);
            else if (id.equals("Blue"))
                return context.getResources().getString(R.string.blue_line_short_name);
            else
                return id;

        } else if (mode == LIGHT_RAIL) {
            if (id.equals("Green-B"))
                return context.getResources().getString(R.string.green_line_b_short_name);
            else if (id.equals("Green-C"))
                return context.getResources().getString(R.string.green_line_c_short_name);
            else if (id.equals("Green-D"))
                return context.getResources().getString(R.string.green_line_d_short_name);
            else if (id.equals("Green-E"))
                return context.getResources().getString(R.string.green_line_e_short_name);
            else if (id.equals("Mattapan"))
                return context.getResources().getString(R.string.red_line_mattapan_short_name);
            else
                return id;

        } else if (mode == BUS) {
            if (id.equals("746"))
                return context.getResources().getString(R.string.silver_line_waterfront_short_name);
            else if (!shortName.equals("") && !shortName.equals("null"))
                return shortName;
            else
                return id;

        } else if (mode == COMMUTER_RAIL) {
            if (id.equals("CapeFlyer")) {
                return context.getResources().getString(R.string.cape_flyer_short_name);
            } else {
                return context.getResources().getString(R.string.commuter_rail_short_name);
            }

        } else if (mode == FERRY) {
            return context.getResources().getString(R.string.ferry_short_name);

        } else {
            return id;
        }
    }

    // Returns the language-specific full name of this route
    // Context is required to get the proper translation
    public String getLongDisplayName(Context context) {
        if (mode == BUS) {
            if (longName.contains("Silver Line") || shortName.contains("SL")) {
                return context.getResources().getString((R.string.silver_line_long_name)) +
                        " " + shortName;
            } else {
                return context.getResources().getString(R.string.route_prefix) +
                        " " + shortName;
            }
        } else if (!longName.equals("") && !longName.equals("null")) {
            return longName;
        } else {
            return id;
        }
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
