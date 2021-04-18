package jackwtat.simplembta.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.model.Vehicle;
import jackwtat.simplembta.views.TripDetailPredictionItem;

public class TripDetailRecyclerViewAdapter
        extends RecyclerView.Adapter<TripDetailRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Prediction> predictions = new ArrayList<>();
    private Stop selectedStop = null;
    private int selectedStopSequence = -1;
    private Vehicle vehicle;
    private OnClickListener onClickListener;
    private OnLongClickListener onLongClickListener;

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

        holder.predictionView.clear();

        Prediction prediction = predictions.get(position);

        int stopSequenceType;
        if (predictions.size() == 1) {
            stopSequenceType = TripDetailPredictionItem.ONLY_STOP;
        } else if (position == 0) {
            stopSequenceType = TripDetailPredictionItem.FIRST_STOP;
            holder.predictionView.bold();
        } else if (position == predictions.size() - 1) {
            stopSequenceType = TripDetailPredictionItem.LAST_STOP;
        } else {
            stopSequenceType = TripDetailPredictionItem.INTERMEDIATE_STOP;
        }

        holder.predictionView.enableNextStopIndicator(position == 0);

        if (position + 1 < predictions.size()) {
            holder.predictionView.setPrediction(prediction, predictions.get(position + 1),
                    stopSequenceType, vehicle);
        } else {
            holder.predictionView.setPrediction(prediction, null, stopSequenceType,
                    vehicle);
        }

        if (onClickListener != null) {
            holder.predictionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener != null) {
                        onClickListener.onItemClick(position);
                    }
                }
            });
        }

        if (onLongClickListener != null) {
            holder.predictionView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onLongClickListener != null) {
                        onLongClickListener.onItemLongClick(position);
                    }
                    return true;
                }
            });
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
            if (prediction.getCountdownTime() >= 0) {
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

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void clear() {
        this.predictions.clear();

        notifyDataSetChanged();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public interface OnClickListener {
        void onItemClick(int i);
    }

    public interface OnLongClickListener {
        void onItemLongClick(int i);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TripDetailPredictionItem predictionView;

        ViewHolder(TripDetailPredictionItem v) {
            super(v);
            predictionView = v;
        }
    }
}
