package jackwtat.simplembta.model;

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
    private String primaryColor = "#FFFFFF";
    private String accentColor = "#3191E1";
    private String textColor = "#000000";
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

    // Returns the language-specific short name of this route
    // Context is required to get the proper translation
    public String getShortDisplayName(Context context) {
        if (mode == Mode.HEAVY_RAIL) {
            if (id.equals("Red"))
                return context.getResources().getString(R.string.red_line_short_name);
            else if (id.equals("Orange"))
                return context.getResources().getString(R.string.orange_line_short_name);
            else if (id.equals("Blue"))
                return context.getResources().getString(R.string.blue_line_short_name);
            else
                return id;

        } else if (mode == Mode.LIGHT_RAIL) {
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

        } else if (mode == Mode.BUS) {
            if (id.equals("746"))
                return context.getResources().getString(R.string.silver_line_waterfront_short_name);
            else if (!shortName.equals("") && !shortName.equals("null"))
                return shortName;
            else
                return id;

        } else if (mode == Mode.COMMUTER_RAIL) {
            if (id.equals("CapeFlyer")) {
                return context.getResources().getString(R.string.cape_flyer_short_name);
            } else {
                return context.getResources().getString(R.string.commuter_rail_short_name);
            }

        } else if (mode == Mode.FERRY) {
            return context.getResources().getString(R.string.ferry_short_name);

        } else {
            return id;
        }
    }

    // Returns the language-specific full name of this route
    // Context is required to get the proper translation
    public String getLongDisplayName(Context context) {
        if (mode == Mode.BUS) {
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

    public void addServiceAlert(ServiceAlert alert) {
        alerts.add(alert);
    }

    public void addServiceAlerts(ArrayList<ServiceAlert> alerts) {
        this.alerts.addAll(alerts);
    }

    public ArrayList<ServiceAlert> getServiceAlerts() {
        return alerts;
    }

    public boolean hasServiceAlerts() {
        return alerts != null && alerts.size() > 0;
    }

    public boolean isValidDestination(String destination) {
        if ((id.equals("741") || id.equals("742") || id.equals("743")) &&
                destination.equals("Silver Line Way")) {
            return false;
        }

        return true;
    }

    public boolean hasSameRouteFamily(Route otherRoute) {
        if (id.equals(otherRoute.getId())) {
            return true;
        }

        if ((id.equals("741") || id.equals("742") || id.equals("743") || id.equals("746")) &&
                (otherRoute.getId().equals("741") || otherRoute.getId().equals("742") ||
                        otherRoute.getId().equals("743") || otherRoute.getId().equals("746"))) {
            return true;
        }

        if ((id.equals("749") || id.equals("751")) &&
                (otherRoute.getId().equals("749") || otherRoute.getId().equals("751"))) {
            return true;
        }

        if ((id.equals("34") || id.equals("34E")) &&
                (otherRoute.getId().equals("34") || otherRoute.getId().equals("34E"))) {
            return true;
        }

        if ((id.equals("57") || id.equals("57A")) &&
                (otherRoute.getId().equals("57") || otherRoute.getId().equals("57A"))) {
            return true;
        }

        if ((id.equals("70") || id.equals("70A")) &&
                (otherRoute.getId().equals("70") || otherRoute.getId().equals("70A"))) {
            return true;
        }

        if (id.substring(0, 5).equals("Green") &&
                otherRoute.getId().substring(0, 5).equals("Green")) {
            return true;
        }


        return false;
    }

    @Override
    public int compareTo(@NonNull Route otherRoute) {
        return this.sortOrder - otherRoute.getSortOrder();
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
