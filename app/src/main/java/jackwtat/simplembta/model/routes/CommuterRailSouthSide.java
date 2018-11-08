package jackwtat.simplembta.model.routes;

public class CommuterRailSouthSide extends CommuterRail {
    private String[] ids = {"CR-Fairmount", "CR-Worcester", "CR-Franklin", "CR-Needham",
            "CR-Providence", "CR-Foxboro"};

    public CommuterRailSouthSide() {
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

    public static boolean isSouthSideCommuterRail(String id) {
        return id.contains("CR-Fairmount") ||
                id.contains("CR-Worcester") ||
                id.contains("CR-Franklin") ||
                id.contains("CR-Needham") ||
                id.contains("CR-Providence") ||
                id.contains("CR-Foxboro");
    }
}
