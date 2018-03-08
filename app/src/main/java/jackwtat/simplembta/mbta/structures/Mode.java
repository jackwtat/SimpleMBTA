package jackwtat.simplembta.mbta.structures;

/**
 * Created by jackw on 2/22/2018.
 */

public enum Mode {
    LIGHT_RAIL(0),
    HEAVY_RAIL(1),
    COMMUTER_RAIL(2),
    BUS(3),
    FERRY(4),
    UNKNOWN(99);

    private int type;

    Mode(int type) {
        this.type = type;
    }

    private int getType() {
        return type;
    }

    public static Mode getModeFromType(int type) {
        for (Mode m : Mode.values()) {
            if (type == m.getType()) {
                return m;
            }
        }

        return UNKNOWN;
    }
}