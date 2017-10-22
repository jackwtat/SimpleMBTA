package jackwtat.simplembta.data;

import android.support.annotation.NonNull;

import static java.lang.Double.parseDouble;

/**
 * Created by jackw on 9/7/2017.
 */

public class Route implements Comparable<Route> {
    private String id;
    private String name;
    private int mode;

    public static final class Mode {
        public static final int SUBWAY_LIGHT = 0;
        public static final int SUBWAY_HEAVY = 1;
        public static final int COMMUTER_RAIL = 2;
        public static final int BUS = 3;
        public static final int BOAT = 4;
        public static final int UNKNOWN = 99;
    }

    public static final class Direction {
        public static final int OUTBOUND = 0;
        public static final int INBOUND = 1;
        public static final int UNKNOWN = -1;
        public static final int COUNT = 2;
    }

    private static final String[][] SPECIAL_ROUTES = {
            {"741", "742", "751", "749", "746", "701", "747", "708", "34E", "57A", "70A"},
            {"0.11", "0.12", "0.14", "0.15", "0.19", "0.21", "0.22", "0.23", "34.1", "57.1", "70.1"},
            {"SL1", "SL2", "SL4", "SL5", "SLW", "CT1", "CT2", "CT3", "34E", "57A", "70A"}};

    public static String getName(String routeId, String routeName) {
        if (isSpecialRoute(routeId)) {
            return getSpecialName(routeId);
        } else if (routeId.length() >= 5 && routeId.substring(0, 5).equals("Green")) {
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
        } else if (routeId.length() >= 4 && routeId.substring(0, 4).equals("Boat")) {
            return "Boat";
        } else {
            return routeName;
        }
    }

    private static boolean isSpecialRoute(String routeId) {
        for (int i = 0; i < SPECIAL_ROUTES[0].length; i++) {
            if (routeId.equals(SPECIAL_ROUTES[0][i])) {
                return true;
            }
        }
        return false;
    }

    private static String getSpecialId(String routeId) {
        for (int i = 0; i < SPECIAL_ROUTES[0].length; i++) {
            if (routeId.equals(SPECIAL_ROUTES[0][i])) {
                return SPECIAL_ROUTES[1][i];
            }
        }
        return routeId;
    }

    private static String getSpecialName(String routeId) {
        for (int i = 0; i < SPECIAL_ROUTES[0].length; i++) {
            if (routeId.equals(SPECIAL_ROUTES[0][i])) {
                return SPECIAL_ROUTES[2][i];
            }
        }
        return "";
    }

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
    public int compareTo(@NonNull Route anotherRoute) {
        String anotherId = anotherRoute.getId();
        int anotherMode = anotherRoute.getMode();
        if (this.mode != anotherMode) {
            /* Compare transportation modes
                Mode hierarchy:
                1. Light Rail (Green Line, Mattapan High Speed Line)
                2. Heavy Rail (Blue, Orange, Red Lines)
                3. Commuter Rail
                4. Bus
                    4a. Silver Line Bus
                    4b. Crosstown Bus
                    4c. Local/Express Bus
                5. Boat/Ferry
             */
            return Integer.compare(this.mode, anotherMode);
        } else if (this.mode != Route.Mode.BUS && anotherMode != Mode.BUS) {
            // We've established that both routes are of the same mode
            // If both mode are not buses
            // Then compare the IDs alphabetically
            return this.getId().compareTo(anotherRoute.getId());
        } else {
            // Both routes are buses
            // Compare special bus route IDs
            double thisBusId= parseDouble(getSpecialId(this.id));
            double anotherBusId= parseDouble(getSpecialId(anotherId));

            return Double.compare(thisBusId, anotherBusId);
        }
    }
}
