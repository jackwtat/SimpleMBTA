package jackwtat.simplembta.model.routes;

import jackwtat.simplembta.model.Direction;

public class SilverLineCombined extends SilverLine {
    private String[] ids = {"741", "742", "743", "746", "749", "751"};

    public SilverLineCombined() {
        super("741,742,743,746,749,751");
        setMode(BUS);
        setShortName("SL");
        setLongName("Silver Line");
        setPrimaryColor("7C878E");
        setTextColor("FFFFFF");
        setSortOrder(14);
        setDirection(new Direction(Direction.OUTBOUND, "Outbound"));
        setDirection(new Direction(Direction.INBOUND, "Inbound"));
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
}
