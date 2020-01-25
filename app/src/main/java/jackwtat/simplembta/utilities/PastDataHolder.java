package jackwtat.simplembta.utilities;

import java.util.HashMap;

import jackwtat.simplembta.model.Prediction;

public class PastDataHolder {
    private static PastDataHolder holder;
    private static HashMap<String, Prediction> predictions = new HashMap<>();

    private PastDataHolder() {
    }

    public static synchronized PastDataHolder getHolder() {
        if (holder == null) {
            holder = new PastDataHolder();
        }
        return holder;
    }

    public void add(Prediction prediction) {
        predictions.put(prediction.getId(), prediction);
    }

    public Prediction getPrediction(String id) {
        return predictions.get(id);
    }

    public void clear(){
        predictions.clear();
    }
}
