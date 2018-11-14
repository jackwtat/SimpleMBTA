package jackwtat.simplembta.model.routes;

import jackwtat.simplembta.map.markers.BlueLineStopMarkerFactory;

public class BlueLine extends RailRoute {
    public BlueLine(String id) {
        super(id);
        setPrimaryColor("003da5");
        setTextColor("FFFFFF");
        setStopMarkerFactory(new BlueLineStopMarkerFactory());
    }

    public static boolean isBlueLine(String id) {
        return id.equals("Blue");
    }
}
