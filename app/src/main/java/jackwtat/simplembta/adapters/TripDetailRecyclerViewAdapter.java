package jackwtat.simplembta.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.views.TripDetailPredictionItem;

public class TripDetailRecyclerViewAdapter
        extends RecyclerView.Adapter<TripDetailRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Prediction> predictions = new ArrayList<>();
    private Stop selectedStop = null;
    private int selectedStopSequence = 0;

    public TripDetailRecyclerViewAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(new TripDetailPredictionItem(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull TripDetailRecyclerViewAdapter.ViewHolder holder, int i) {
        final int position = i;
        boolean showCountdown = predictions.get(0).getCountdownTime() < 3600000;

        holder.predictionView.clear();

        Prediction prediction = predictions.get(position);

        int stopSequence;
        if (predictions.size() == 1) {
            stopSequence = TripDetailPredictionItem.ONLY_STOP;
        } else if (position == 0) {
            stopSequence = TripDetailPredictionItem.FIRST_STOP;
        } else if (position == predictions.size() - 1) {
            stopSequence = TripDetailPredictionItem.LAST_STOP;
        } else {
            stopSequence = TripDetailPredictionItem.INTERMEDIATE_STOP;
        }

        holder.predictionView.setPrediction(prediction, stopSequence, showCountdown);

        if (prediction.getStop().equals(selectedStop)) {
            holder.predictionView.emphasize();
        }
    }

    @Override
    public int getItemCount() {
        return predictions.size();
    }

    public Prediction getPrediction(int position) {
        return predictions.get(position);
    }

    public void setPredictions(List<Prediction> predictions) {
        this.predictions.clear();

        for (Prediction prediction : predictions) {
            if (prediction.getPredictionTime() != null &&
                    prediction.getStopSequence() >= selectedStopSequence) {
                prediction.setSortMethod(Prediction.STOP_SEQUENCE);
                this.predictions.add(prediction);
            }
        }

        Collections.sort(this.predictions);

        notifyDataSetChanged();
    }

    public void setSelectedStop(Stop stop) {
        this.selectedStop = stop;
    }

    public void setSelectedStopSequence(int selectedStopSequence) {
        this.selectedStopSequence = selectedStopSequence;
    }

    public void clear() {
        this.predictions.clear();

        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TripDetailPredictionItem predictionView;

        ViewHolder(TripDetailPredictionItem v) {
            super(v);
            predictionView = v;
        }
    }
}
