package jackwtat.simplembta.model.routes;

import jackwtat.simplembta.map.markers.SilverLineStopMarkerFactory;

public class SilverLine extends Bus {
    public SilverLine(String id) {
        super(id);
        setPrimaryColor("7C878E");
        setTextColor("FFFFFF");
        setStopMarkerFactory(new SilverLineStopMarkerFactory());
    }

    public static boolean isSilverLine(String id) {
        return id.equals("741") ||
                id.equals("742") ||
                id.equals("743") ||
                id.equals("746") ||
                id.equals("749") ||
                id.equals("751");
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
