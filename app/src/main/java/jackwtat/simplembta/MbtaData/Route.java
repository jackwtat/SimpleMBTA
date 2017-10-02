package jackwtat.simplembta.MbtaData;

import android.support.annotation.NonNull;

/**
 * Created by jackw on 9/7/2017.
 */

public class Route implements Comparable {
    public static final int OUTBOUND = 0;
    public static final int INBOUND = 1;
    public static final int UNKNOWN = -1;
    public static final int[] DIRECTIONS = {INBOUND, OUTBOUND};

    private String id;
    private String name;
    private int mode;

    public Route(String id, String name, int mode) {
        this.id = id;
        this.name = name;
        this.mode = mode;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMode() {
        return mode;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        // TODO: Implement compareTo
        return 0;
    }

    public static String getShortName(String id) {
        if (id.substring(0, 1).equals("G")) {
            return "GL-" + id.substring(6, 7);
        } else if (id.equals("Mattapan")) {
            return "RL-M";
        } else if (id.equals("Blue")) {
            return "BL";
        } else if (id.equals("Orange")) {
            return "OL";
        } else if (id.equals("Red")) {
            return "RL";
        } else if (id.substring(0, 1).equals("C")) {
            return "CR";
        } else if (id.equals("701")) {
            return "CT1";
        } else if (id.equals("747")) {
            return "CT2";
        } else if (id.equals("708")) {
            return "CT3";
        } else if (id.equals("741")) {
            return "SL1";
        } else if (id.equals("742")) {
            return "SL2";
        } else if (id.equals("751")) {
            return "SL4";
        } else if (id.equals("749")) {
            return "SL5";
        } else if (id.equals("746")) {
            return "SL-W";
        } else if (id.equals("Boat-F4")) {
            return "F4";
        } else if (id.equals("Boat-F1")) {
            return "F1";
        } else {
            return id;
        }
    }
}
