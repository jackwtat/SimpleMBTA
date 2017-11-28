package jackwtat.simplembta.mbta.data;

import android.support.annotation.NonNull;

/**
 * Created by jackw on 11/27/2017.
 */

public class Route implements Comparable<Route> {
    private String id;
    private String shortName;
    private String longName;
    private String color;
    private int sortOrder;

    public Route(String id){
        this.id = id;
    }

    public Route(String id, String shortName, String longName, String color, int sortOrder) {
        this.id = id;
        this.shortName = shortName;
        this.longName = longName;
        this.color = color;
        this.sortOrder = sortOrder;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public int compareTo(@NonNull Route route) {
        return Integer.compare(this.sortOrder, route.getSortOrder());
    }
}
