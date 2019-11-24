package jackwtat.simplembta.utilities;

import java.util.HashMap;

import jackwtat.simplembta.model.Prediction;

public class PastPredictionsHolder {
    private static PastPredictionsHolder holder;
    private static HashMap<String, Prediction> predictions = new HashMap<>();

    private PastPredictionsHolder() {
    }

    public static synchronized PastPredictionsHolder getHolder() {
        if (holder == null) {
            holder = new PastPredictionsHolder();
        }
        return holder;
    }

    public void add(Prediction prediction) {
        predictions.put(prediction.getId(), prediction);
    }

    public Prediction get(String id) {
        return predictions.get(id);
    }

    public void clear(){
        predictions.clear();
    }
}
