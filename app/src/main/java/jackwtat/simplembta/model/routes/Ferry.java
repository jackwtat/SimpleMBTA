package jackwtat.simplembta.model.routes;

import jackwtat.simplembta.map.FerryStopMarkerFactory;

public class Ferry extends Route {
    public Ferry(String id) {
        super(id);
        setMode(FERRY);
        setPrimaryColor("008eaa");
        setTextColor("FFFFFF");
        setStopMarkerFactory(new FerryStopMarkerFactory());
    }
}
