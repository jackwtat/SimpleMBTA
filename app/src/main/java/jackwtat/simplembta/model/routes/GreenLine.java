package jackwtat.simplembta.model.routes;

import jackwtat.simplembta.map.markers.GreenLineStopMarkerFactory;

public class GreenLine extends Route {
    public GreenLine(String id) {
        super(id);
        setPrimaryColor("00843D");
        setTextColor("FFFFFF");
        setStopMarkerFactory(new GreenLineStopMarkerFactory());
    }

    public static boolean isGreenLine(String id) {
        return id.contains("Green-B") ||
                id.contains("Green-C") ||
                id.contains("Green-D") ||
                id.contains("Green-E");
    }

    public static boolean isGreenLineB(String id){
        return id.equals("Green-B");
    }

    public static boolean isGreenLineC(String id){
        return id.equals("Green-C");
    }

    public static boolean isGreenLineD(String id){
        return id.equals("Green-D");
    }

    public static boolean isGreenLineE(String id){
        return id.equals("Green-E");
    }
}
