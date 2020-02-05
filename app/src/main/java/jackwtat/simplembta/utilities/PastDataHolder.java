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

    public Prediction normalizePrediction(Prediction p) {
        Prediction priorPrediction = predictions.get(p.getId());
        if (priorPrediction != null) {
            long thisCountdown = p.getCountdownTime();
            long priorCountdown = priorPrediction.getCountdownTime();
            long timeDifference = thisCountdown - priorCountdown;

            if (priorCountdown < 30000 || (timeDifference < 90000 && timeDifference > 0)) {
                p.setArrivalTime(priorPrediction.getArrivalTime());
                p.setDepartureTime(priorPrediction.getDepartureTime());
            }
        }

        return p;
    }

    public void clear() {
        predictions.clear();
    }
}
