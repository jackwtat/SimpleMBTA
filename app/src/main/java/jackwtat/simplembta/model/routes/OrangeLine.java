package jackwtat.simplembta.model.routes;

import jackwtat.simplembta.map.markers.OrangeLineStopMarkerFactory;

public class OrangeLine extends Route {
    public OrangeLine(String id) {
        super(id);
        setPrimaryColor("ed8b00");
        setTextColor("FFFFFF");
        setStopMarkerFactory(new OrangeLineStopMarkerFactory());
    }

    public static boolean isOrangeLine(String id) {
        return id.equals("Orange");
    }
}
