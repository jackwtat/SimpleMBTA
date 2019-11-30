package jackwtat.simplembta.model.routes;

import jackwtat.simplembta.map.OrangeLineStopMarkerFactory;

public class OrangeLine extends RailRoute {
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
