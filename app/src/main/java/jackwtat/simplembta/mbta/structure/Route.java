package jackwtat.simplembta.mbta.structure;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by jackw on 11/27/2017.
 */

public class Route implements Comparable<Route> {
    private String id = "";
    private Mode mode = Mode.UNKNOWN;
    private String shortName = "";
    private String longName = "";
    private String color = "FFFFFF";
    private String textColor = "000000";
    private int sortOrder = -1;
    private ArrayList<ServiceAlert> alerts = new ArrayList<>();

    public Route(String id) {
        this.id = id;
    }

    public Route(String id, Mode mode, String shortName, String longName, String color, String textColor,
                 int sortOrder) {
        this.id = id;
        this.mode = mode;
        this.shortName = shortName;
        this.longName = longName;
        this.color = color;
        this.textColor = textColor;
        this.sortOrder = sortOrder;
    }

    public String getId() {
        return id;
    }

    public Mode getMode() {
        return mode;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public String getColor() {
        return color;
    }

    public String getTextColor() {
        return textColor;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void addServiceAlert(ServiceAlert alert) {
        alerts.add(alert);
    }

    public void addServiceAlerts(ArrayList<ServiceAlert> alerts) {
        this.alerts.addAll(alerts);
    }

    public ArrayList<ServiceAlert> getServiceAlerts() {
        return alerts;
    }

    @Override
    public int compareTo(@NonNull Route route) {
        return Integer.compare(this.sortOrder, route.getSortOrder());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Route) {
            Route route = (Route) obj;
            return this.id.equals(route.getId());
        } else {
            return false;
        }
    }
}
