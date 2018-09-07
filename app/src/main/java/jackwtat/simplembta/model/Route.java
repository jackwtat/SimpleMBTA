package jackwtat.simplembta.model;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import jackwtat.simplembta.R;

public class Route implements Comparable<Route> {
    public static final int INBOUND = 1;
    public static final int OUTBOUND = 0;
    public static final int NULL_DIRECTION = -1;

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

    private Stop nearestInboundStop = null;
    private Stop nearestOutboundStop = null;
    private ArrayList<Prediction> inboundPredictions = new ArrayList<>();
    private ArrayList<Prediction> outboundPredictions = new ArrayList<>();

    public Route(String id) {
        this.id = id;
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

    public Stop getNearestStop(int direction) {
        if (direction == 1) {
            return nearestInboundStop;
        } else if (direction == 0) {
            return nearestOutboundStop;
        } else {
            return null;
        }
    }

    public ArrayList<Prediction> getPredictions(int direction) {
        if (direction == 1) {
            return inboundPredictions;
        } else if (direction == 0) {
            return outboundPredictions;
        } else {
            return new ArrayList<>();
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

    public void setNearestStop(int direction, Stop stop) {
        if (direction == 1) {
            if (nearestInboundStop == null || !nearestInboundStop.equals(stop)) {
                inboundPredictions.clear();
            }

            nearestInboundStop = stop;

        } else {
            if (nearestOutboundStop == null || !nearestOutboundStop.equals(stop)) {
                outboundPredictions.clear();
            }

            nearestOutboundStop = stop;
        }
    }

    public boolean hasPredictions() {
        return inboundPredictions.size() > 0 || outboundPredictions.size() > 0;
    }

    public boolean hasNearbyStops() {
        return nearestInboundStop != null || nearestOutboundStop != null;
    }

    public void addPrediction(Prediction prediction) {
        if (prediction.getDirection() == 1) {
            inboundPredictions.add(prediction);
        } else {
            outboundPredictions.add(prediction);
        }
    }

    public void addPredictions(List<Prediction> predictions) {
        if (predictions.get(0).getDirection() == 1) {
            inboundPredictions.addAll(predictions);
        } else {
            outboundPredictions.addAll(predictions);
        }
    }

    public void addServiceAlert(ServiceAlert serviceAlert) {
        serviceAlerts.add(serviceAlert);
    }

    public void addServiceAlerts(List<ServiceAlert> serviceAlerts) {
        this.serviceAlerts.addAll(serviceAlerts);
    }

    @Override
    public int compareTo(@NonNull Route otherRoute) {
        return this.sortOrder - otherRoute.getSortOrder();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Route) {
            Route otherRoute = (Route) obj;
            return id.equals(otherRoute.getId());
        } else {
            return false;
        }
    }
}
