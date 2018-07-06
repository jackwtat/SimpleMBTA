package jackwtat.simplembta.mbta.structure;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import jackwtat.simplembta.R;

/**
 * Created by jackw on 11/27/2017.
 */

public class Route implements Comparable<Route> {
    private String id = "";
    private Mode mode = Mode.UNKNOWN;
    private String shortName = "";
    private String longName = "";
    private String primaryColor = "FFFFFF";
    private String accentColor = "3191E1";
    private String textColor = "000000";
    private int sortOrder = -1;
    private ArrayList<ServiceAlert> alerts = new ArrayList<>();

    public Route(String id) {
        this.id = id;
    }

    public Route(String id, Mode mode, String shortName, String longName, String primaryColor,
                 String textColor, int sortOrder) {
        this.id = id;
        this.mode = mode;
        this.shortName = shortName;
        this.longName = longName;
        this.primaryColor = primaryColor;
        this.textColor = textColor;
        this.sortOrder = sortOrder;
    }

    public Route(String id, Mode mode, String shortName, String longName, String primaryColor,
                 String accentColor, String textColor, int sortOrder) {
        this.id = id;
        this.mode = mode;
        this.shortName = shortName;
        this.longName = longName;
        this.primaryColor = primaryColor;
        this.accentColor = accentColor;
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

    // Returns the language-specific full name of this route
    // Context is required to get the proper translation
    public String getDisplayName(Context context) {
        if (mode == Mode.BUS && !longName.contains("Silver Line")) {
            return context.getResources().getString(R.string.route_prefix) + " " + shortName;
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
