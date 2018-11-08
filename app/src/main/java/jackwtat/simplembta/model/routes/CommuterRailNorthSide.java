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

    public static boolean isNorthSideCommuterRail(String id) {
        return id.contains("CR-Fitchburg") ||
                id.contains("CR-Haverhill") ||
                id.contains("CR-Lowell") ||
                id.contains("CR-Newburyport");
    }
}
