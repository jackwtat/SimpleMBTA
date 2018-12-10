package jackwtat.simplembta.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "favorites")
public class Favorite {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "route_id")
    public String routeId;

    @ColumnInfo(name = "stop_id")
    public String stopId;

    @ColumnInfo(name = "direction_id")
    public int directionId;

    public Favorite(String routeId, String stopId, int directionId) {
        this.routeId = routeId;
        this.stopId = stopId;
        this.directionId = directionId;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getStopId() {
        return stopId;
    }

    public int getDirectionId() {
        return directionId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public void setDirectionId(int directionId) {
        this.directionId = directionId;
    }
}
