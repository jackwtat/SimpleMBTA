package jackwtat.simplembta.MbtaData;

import android.support.annotation.NonNull;

/**
 * Created by jackw on 9/7/2017.
 */

public class Route implements Comparable<Route> {
    private String id;
    private String name;
    private int mode;

    public class Mode {
        public static final int SUBWAY_LIGHT = 0;
        public static final int SUBWAY_HEAVY = 1;
        public static final int COMMUTER_RAIL = 2;
        public static final int BUS = 3;
        public static final int BOAT = 4;
        public static final int UNKNOWN = 99;
    }

    public class Direction {
        public static final int OUTBOUND = 0;
        public static final int INBOUND = 1;
        public static final int UNKNOWN = -1;
        public static final int COUNT = 2;
    }

    public static String getRouteName(String routeId) {
        if (isSpecialBusRoute(routeId)) {
            return getSpecialBusRouteName(routeId);
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
            return routeId;
        }
    }

    private static final String[][] SPECIAL_ROUTES = {
            {"741", "742", "751", "749", "746", "701", "747", "708"},
            {"SL1", "SL2", "SL4", "SL5", "SL-W", "CT1", "CT2", "CT3"}};

    private static boolean isSpecialBusRoute(String routeId) {
        for (int i = 0; i < SPECIAL_ROUTES.length; i++) {
            if (routeId.equals(SPECIAL_ROUTES[i][0])) {
                return true;
            }
        }
        return false;
    }

    private static String getSpecialBusRouteName(String routeId) {
        for (int i = 0; i < SPECIAL_ROUTES.length; i++) {
            if (routeId.equals(SPECIAL_ROUTES[i][0])) {
                return SPECIAL_ROUTES[i][1];
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
        /*
        String anotherId = anotherRoute.getId();
        int anotherMode = anotherRoute.getMode();
        if (this.id == anotherId){
            // Both are the same route
            return 0;
        } if (this.mode < anotherMode) {
            // This route is 
            return -1;
        } else if (this.mode > anotherMode) {
            return 1;
            // We've established that both routes are of the same mode
        } else if (this.mode != Route.Mode.BUS && anotherMode != Mode.BUS){
            // If Both routes are not buses
            // Then compare the IDs alphabetically
            return this.getId().compareTo(anotherRoute.getId());
        } else {
            // Both routes are buses
            // Special bus routes are before non-special bus routes
            if(isSpecialBusRoute(this.id) && !isSpecialBusRoute(anotherRoute.getId())){
                return -1;
            } else if (!isSpecialBusRoute(this.id) && isSpecialBusRoute(anotherRoute.getId())){
                return 1;
            } else if (isSpecialBusRoute(this.id) && isSpecialBusRoute(anotherRoute.getId())){
                // Both routes are special bus routes
                // Compare the corresponding special route rankings
                int thisSpecialBusRank = getSpecialBusRouteSortingRank(this.id);
                int anotherSpecialBusRank = getSpecialBusRouteSortingRank(anotherId);
                if () {
                    
                }
            } else {
                // Both routes are non-special bus routes
                // Convert IDs to integers and compare
                
            }
        }
        */
        return 0;
    }

    private static int getSortingRank(String routeId){
        for (int i = 0; i < SPECIAL_ROUTES.length; i++){
            if (routeId.equals(SPECIAL_ROUTES[i][0])){
                return i;
            }
        }
        return -1;
    }
}
