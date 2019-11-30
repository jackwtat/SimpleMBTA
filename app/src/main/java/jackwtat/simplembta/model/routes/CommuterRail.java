package jackwtat.simplembta.model.routes;

import jackwtat.simplembta.map.CommuterRailStopMarkerFactory;

public class CommuterRail extends RailRoute {
    public CommuterRail(String id) {
        super(id);
        setPrimaryColor("80276C");
        setTextColor("FFFFFF");
        setStopMarkerFactory(new CommuterRailStopMarkerFactory());
    }

    public static boolean isCommuterRailHub(String stopId, boolean includeTerminals) {
        return (includeTerminals && (stopId.equals("place-north") || stopId.equals("place-sstat"))) ||
                stopId.equals("place-bbsta") ||
                stopId.equals("place-rugg") ||
                stopId.equals("place-jfk") ||
                stopId.equals("place-qnctr");
    }
}
