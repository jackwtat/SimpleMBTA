package jackwtat.simplembta.model;

public class Routes {
    public static class GreenLineGroup extends Route {
        String[] ids = {"Green-B", "Green-C", "Green-D", "Green-E"};

        public GreenLineGroup() {
            super("Green-B,Green-C,Green-D,Green-E");
            setMode(LIGHT_RAIL);
            setShortName("GL");
            setLongName("Green Line");
            setPrimaryColor("00843D");
            setTextColor("FFFFFF");
            setSortOrder(4);

            String[] directionNames = {"Westbound", "Eastbound"};
            setDirectionNames(directionNames);
        }

        @Override
        public boolean idEquals(String routeId) {
            for (String id : ids) {
                if (id.equals(routeId)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static class NorthSideCommuterRail extends Route {
        String[] ids = {"CR-Fitchburg", "CR-Haverhill", "CR-Lowell", "CR-Newburyport"};

        public NorthSideCommuterRail() {
            super("CR-Fitchburg,CR-Haverhill,CR-Lowell,CR-Newburyport");
            setMode(COMMUTER_RAIL);
            setShortName("CR");
            setLongName("Commuter Rail");
            setPrimaryColor("80276C");
            setTextColor("FFFFFF");
            setSortOrder(50);
        }

        @Override
        public boolean idEquals(String routeId) {
            for (String id : ids) {
                if (id.equals(routeId)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static class SouthSideCommuterRail extends Route {
        String[] ids = {"CR-Fairmount", "CR-Worcester", "CR-Franklin", "CR-Needham",
                "CR-Providence", "CR-Foxboro"};

        public SouthSideCommuterRail() {
            super("CR-Fairmount,CR-Worcester,CR-Franklin,CR-Needham,CR-Providence,CR-Foxboro");
            setMode(COMMUTER_RAIL);
            setShortName("CR");
            setLongName("Commuter Rail");
            setPrimaryColor("80276C");
            setTextColor("FFFFFF");
            setSortOrder(50);
        }

        @Override
        public boolean idEquals(String routeId) {
            for (String id : ids) {
                if (id.equals(routeId)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static class OldColonyCommuterRail extends Route {
        String[] ids = {"CR-Greenbush", "CR-Kingston", "CR-Middleborough"};

        public OldColonyCommuterRail() {
            super("CR-Greenbush,CR-Kingston,CR-Middleborough");
            setMode(COMMUTER_RAIL);
            setShortName("CR");
            setLongName("Commuter Rail");
            setPrimaryColor("80276C");
            setTextColor("FFFFFF");
            setSortOrder(50);
        }

        @Override
        public boolean idEquals(String routeId) {
            for (String id : ids) {
                if (id.equals(routeId)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean isParentOf(String parentId, String childId) {
        if (parentId.equals("2427") && (childId.equals("24") || childId.equals("27")))
            return true;
        else if (parentId.equals("3233") && (childId.equals("32") || childId.equals("33")))
            return true;
        else if (parentId.equals("3738") && (childId.equals("37") || childId.equals("38")))
            return true;
        else if (parentId.equals("4050") && (childId.equals("40") || childId.equals("50")))
            return true;
        else if (parentId.equals("627") && (childId.equals("62") || childId.equals("76")))
            return true;
        else if (parentId.equals("725") && (childId.equals("72") || childId.equals("75")))
            return true;
        else if (parentId.equals("8993") && (childId.equals("89") || childId.equals("93")))
            return true;
        else if (parentId.equals("116117") && (childId.equals("116") || childId.equals("117")))
            return true;
        else if (parentId.equals("214216") && (childId.equals("214") || childId.equals("216")))
            return true;
        else if (parentId.equals("441442") && (childId.equals("441") || childId.equals("442")))
            return true;
        else return false;
    }

    public static boolean isSilverLine(String id) {
        return id.equals("741") ||
                id.equals("742") ||
                id.equals("743") ||
                id.equals("746") ||
                id.equals("749") ||
                id.equals("751");
    }

    public static boolean isGreenLine(String id) {
        return id.equals("Green-B") ||
                id.equals("Green-C") ||
                id.equals("Green-D") ||
                id.equals("Green-E") ||
                id.equals("Green-B,Green-C,Green-D,Green-E");
    }

    public static boolean isNorthSideCommuterRail(String id) {
        return id.equals("CR-Fitchburg") ||
                id.equals("CR-Haverhill") ||
                id.equals("CR-Lowell") ||
                id.equals("CR-Newburyport");
    }

    public static boolean isSouthSideCommuterRail(String id) {
        return id.equals("CR-Fairmount") ||
                id.equals("CR-Worcester") ||
                id.equals("CR-Franklin") ||
                id.equals("CR-Needham") ||
                id.equals("CR-Providence") ||
                id.equals("CR-Foxboro");
    }

    public static boolean isOldColonyCommuterRail(String id) {
        return id.equals("CR-Greenbush") ||
                id.equals("CR-Kingston") ||
                id.equals("CR-Middleborough");
    }
}
