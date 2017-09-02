package jackwtat.simplembta;

import java.util.ArrayList;

/**
 * Created by jackw on 8/26/2017.
 */

public class Stop {
    private String id;
    private String name;
    private float latitude;
    private float longitude;
    private ArrayList<Prediction> predictions;

    public Stop(String id, String name, float latitude, float longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {return id;}

    public String getName() {
        return name;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public ArrayList<Prediction> getPredictions(){
        return predictions;
    }

    public void refreshPredictions(){

    }
}
