package jackwtat.simplembta.model.routes;

import jackwtat.simplembta.model.Direction;

public class GreenLineCombined extends GreenLine {
    private String[] ids = {"Green-B", "Green-C", "Green-D", "Green-E"};

    public GreenLineCombined() {
        super("Green-B,Green-C,Green-D,Green-E");
        setMode(LIGHT_RAIL);
        setShortName("GL");
        setLongName("Green Line");
        setPrimaryColor("00843D");
        setTextColor("FFFFFF");
        setSortOrder(4);
        setDirection(new Direction(Direction.WESTBOUND, "Westbound"));
        setDirection(new Direction(Direction.EASTBOUND, "Eastbound"));
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
