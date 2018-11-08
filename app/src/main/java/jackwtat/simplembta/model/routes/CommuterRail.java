package jackwtat.simplembta.model.routes;

import jackwtat.simplembta.map.markers.CommuterRailStopMarkerFactory;

public class CommuterRail extends Route {
    public CommuterRail(String id) {
        super(id);
        setPrimaryColor("80276C");
        setTextColor("FFFFFF");
        setStopMarkerFactory(new CommuterRailStopMarkerFactory());
    }
}
