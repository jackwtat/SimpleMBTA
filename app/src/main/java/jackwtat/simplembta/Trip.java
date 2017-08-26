package jackwtat.simplembta;

/**
 * Created by jackw on 8/26/2017.
 */

public class Trip {
    private String id;
    private String name;
    private String headsign;
    private int mode;
    private long arrivalTime;

    public Trip (String id, String name, String headsign, int mode, long arrivalTime) {
        this.id = id;
        this.name = name;
        this.headsign = headsign;
        this.mode = mode;
        this.arrivalTime = arrivalTime;
    }

    public boolean hasId(String id) {
        return id.equals(this.id);
    }

    public String getName() {
        return name;
    }

    public String getHeadsign() {
        return headsign;
    }

    public int getMode() {
        return mode;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }
}
