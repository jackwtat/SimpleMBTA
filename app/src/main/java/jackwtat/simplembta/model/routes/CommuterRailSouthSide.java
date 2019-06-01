package jackwtat.simplembta.model.routes;

public class CommuterRailSouthSide extends CommuterRail {
    private String[] ids = {"CR-Fairmount", "CR-Worcester", "CR-Franklin", "CR-Needham",
            "CR-Providence", "CR-Foxboro"};

    public CommuterRailSouthSide() {
        super("CR-Fairmount,CR-Worcester,CR-Franklin,CR-Needham,CR-Providence,CR-Foxboro");
        setMode(COMMUTER_RAIL);
        setShortName("CR");
        setLongName("Commuter Rail - South Side");
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

    public static boolean isSouthSideCommuterRail(String id) {
        return id.contains("CR-Fairmount") ||
                id.contains("CR-Worcester") ||
                id.contains("CR-Franklin") ||
                id.contains("CR-Needham") ||
                id.contains("CR-Providence") ||
                id.contains("CR-Foxboro");
    }
}
