package jackwtat.simplembta;

import java.util.ArrayList;
import java.util.List;

import jackwtat.simplembta.Utils.QueryUtil;

/**
 * Created by jackw on 8/26/2017.
 */

public class Stop {
    private String id;
    private String name;
    private float latitude;
    private float longitude;
    private List<Route> routes;
    private List<Prediction> predictions;

    public Stop(String id, String name, float latitude, float longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public List<Route> getRoutes() { return routes; }

    public List<Prediction> getPredictions() {
        return predictions;
    }

    public void refreshRoutes() { routes = QueryUtil.fetchRoutesByStop(id); }

    public void refreshPredictions() {
        predictions = QueryUtil.fetchPredictionsByStop(id);
    }
}
