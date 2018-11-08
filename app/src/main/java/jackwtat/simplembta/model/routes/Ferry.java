package jackwtat.simplembta.model.routes;

import jackwtat.simplembta.map.markers.FerryStopMarkerFactory;

public class Ferry extends Route {
    public Ferry(String id) {
        super(id);
        setPrimaryColor("008eaa");
        setTextColor("FFFFFF");
        setStopMarkerFactory(new FerryStopMarkerFactory());
    }
}
