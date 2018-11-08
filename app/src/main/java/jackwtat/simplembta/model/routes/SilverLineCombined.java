package jackwtat.simplembta.model.routes;

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

        String[] directionNames = {"Inbound", "Outbound"};
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
