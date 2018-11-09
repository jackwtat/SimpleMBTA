package jackwtat.simplembta.model.routes;

public class CommuterRailOldColony extends CommuterRail {
    private String[] ids = {"CR-Greenbush", "CR-Kingston", "CR-Middleborough"};

    public CommuterRailOldColony() {
        super("CR-Greenbush,CR-Kingston,CR-Middleborough");
        setMode(COMMUTER_RAIL);
        setShortName("CR");
        setLongName("Commuter Rail");
        setPrimaryColor("80276C");
        setTextColor("FFFFFF");
        setSortOrder(50);
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

    public static boolean isOldColonyCommuterRail(String id) {
        return id.contains("CR-Greenbush") ||
                id.contains("CR-Kingston") ||
                id.contains("CR-Middleborough");
    }
}
