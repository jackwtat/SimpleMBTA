package jackwtat.simplembta.model.routes;

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
