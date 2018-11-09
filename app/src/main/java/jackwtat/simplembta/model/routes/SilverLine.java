package jackwtat.simplembta.model.routes;

import jackwtat.simplembta.map.markers.SilverLineStopMarkerFactory;
import jackwtat.simplembta.model.Prediction;

public class SilverLine extends Bus {
    public SilverLine(String id) {
        super(id);
        setPrimaryColor("7C878E");
        setTextColor("FFFFFF");
        setStopMarkerFactory(new SilverLineStopMarkerFactory());
    }

    @Override
    public void addPrediction(Prediction prediction) {
        // Silver Line Way trips are often duplicated under SL1 (741), SL2 (742), and SL3 (743)
        if (!prediction.getDestination().equals("Silver Line Way") || prediction.getRouteId().equals("746"))
            super.addPrediction(prediction);
    }

    public static boolean isSilverLine(String routeId) {
        return isSL1(routeId) ||
                isSL2(routeId) ||
                isSL3(routeId) ||
                isSL4(routeId) ||
                isSL5(routeId) ||
                isSLW(routeId);
    }

    public static boolean isSL1(String id) {
        return id.equals("741");
    }

    public static boolean isSL2(String id) {
        return id.equals("742");
    }

    public static boolean isSL3(String id) {
        return id.equals("743");
    }

    public static boolean isSL4(String id) {
        return id.equals("751");
    }

    public static boolean isSL5(String id) {
        return id.equals("749");
    }

    public static boolean isSLW(String id) {
        return id.equals("746");
    }
}
