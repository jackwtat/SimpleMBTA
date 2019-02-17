package jackwtat.simplembta.model.routes;

public class CommuterRailNorthSide extends CommuterRail {
    private String[] ids = {"CR-Fitchburg", "CR-Haverhill", "CR-Lowell", "CR-Newburyport"};

    public CommuterRailNorthSide() {
        super("CR-Fitchburg,CR-Haverhill,CR-Lowell,CR-Newburyport");
        setMode(COMMUTER_RAIL);
        setShortName("CR");
        setLongName("Commuter Rail");
        setPrimaryColor("80276C");
        setTextColor("FFFFFF");
        setSortOrder(20000);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Route) {
            Route otherRoute = (Route) obj;
            for (String id : ids) {
                if (id.equals(otherRoute.getId()))
                    return true;
            }

            return getId().equals(otherRoute.getId());

        } else if (obj instanceof String) {
            for (String id : ids) {
                if (id.equals(obj))
                    return true;
            }

            return getId().equals(obj);

        } else {
            return false;
        }
    }

    public static boolean isNorthSideCommuterRail(String id) {
        return id.contains("CR-Fitchburg") ||
                id.contains("CR-Haverhill") ||
                id.contains("CR-Lowell") ||
                id.contains("CR-Newburyport");
    }
}
