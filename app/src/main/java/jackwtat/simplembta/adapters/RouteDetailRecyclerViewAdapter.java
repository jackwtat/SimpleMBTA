package jackwtat.simplembta.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.views.RouteDetailPredictionItem;

public class RouteDetailRecyclerViewAdapter
        extends RecyclerView.Adapter<RouteDetailRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Prediction> predictions;

    private OnItemClickListener onItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        RouteDetailPredictionItem predictionView;

        ViewHolder(RouteDetailPredictionItem v) {
            super(v);
            predictionView = v;
        }
    }

    public RouteDetailRecyclerViewAdapter() {
        predictions = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(new RouteDetailPredictionItem(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull RouteDetailRecyclerViewAdapter.ViewHolder holder, int position) {
        final int i = position;

        holder.predictionView.clear();
        holder.predictionView.setPrediction(predictions.get(position));

        holder.predictionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(i);
                }
            }
        });
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
            if (prediction.getPredictionTime() != null && prediction.getCountdownTime() >= 0) {
                this.predictions.add(prediction);
            }
        }

        Collections.sort(this.predictions);

        notifyDataSetChanged();
    }

    public void clear() {
        this.predictions.clear();
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int i);
    }
}
