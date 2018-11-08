package jackwtat.simplembta.model;

import java.io.Serializable;

public class Direction implements Serializable {
    public static final int OUTBOUND = 0;
    public static final int INBOUND = 1;
    public static final int WESTBOUND = 0;
    public static final int EASTBOUND = 1;
    public static final int SOUTHBOUND = 0;
    public static final int NORTHBOUND = 1;
    public static final int NULL_DIRECTION = 0;

    int id;
    String name;

    public Direction(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Direction) {
            Direction otherDirection = (Direction) obj;
            return id == otherDirection.id;
        } else {
            return false;
        }
    }
}
