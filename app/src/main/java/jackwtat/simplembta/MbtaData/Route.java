package jackwtat.simplembta.MbtaData;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import jackwtat.simplembta.R;

/**
 * Created by jackw on 9/7/2017.
 */

public class Route implements Comparable {
    public static final int OUTBOUND = 0;
    public static final int INBOUND = 1;
    public static final int UNKNOWN = -1;
    public static final int[] DIRECTIONS = {INBOUND, OUTBOUND};

    private String id;
    private String name;
    private int mode;

    public Route(String id, String name, int mode) {
        this.id = id;
        this.name = name;
        this.mode = mode;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMode() {
        return mode;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        // TODO: Implement compareTo
        return 0;
    }

    public static String getShortName(String routeId) {
        if (routeId.length() >= 5 && routeId.substring(0, 5).equals("Green")) {
            return "GL-" + routeId.substring(6, 7);
        } else if (routeId.equals("Mattapan")) {
            return "RL-M";
        } else if (routeId.equals("Blue")) {
            return "BL";
        } else if (routeId.equals("Orange")) {
            return "OL";
        } else if (routeId.equals("Red")) {
            return "RL";
        } else if (routeId.length() >= 2 && routeId.substring(0, 2).equals("CR")) {
            return "CR";
        } else if (routeId.equals("701")) {
            return "CT1";
        } else if (routeId.equals("747")) {
            return "CT2";
        } else if (routeId.equals("708")) {
            return "CT3";
        } else if (routeId.equals("741")) {
            return "SL1";
        } else if (routeId.equals("742")) {
            return "SL2";
        } else if (routeId.equals("751")) {
            return "SL4";
        } else if (routeId.equals("749")) {
            return "SL5";
        } else if (routeId.equals("746")) {
            return "SL-W";
        } else if (routeId.length() >= 4 && routeId.substring(0, 4).equals("Boat")) {
            return "Boat";
        } else {
            return routeId;
        }
    }


}
