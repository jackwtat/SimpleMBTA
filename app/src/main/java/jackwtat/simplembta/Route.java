package jackwtat.simplembta;

/**
 * Created by jackw on 9/7/2017.
 */

public class Route {
    public static final int OUTBOUND = 0;
    public static final int INBOUND = 1;

    private String id;
    private String name;
    private int mode;

    public Route(String id, String name, int mode){
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
}
