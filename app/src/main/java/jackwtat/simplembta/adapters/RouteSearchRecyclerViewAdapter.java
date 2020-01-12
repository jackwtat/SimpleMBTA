package jackwtat.simplembta.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.views.RouteSearchPredictionItem;

public class RouteSearchRecyclerViewAdapter
        extends RecyclerView.Adapter<RouteSearchRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Prediction> predictions = new ArrayList<>();
    private OnItemClickListener onItemClickListener = null;

    public RouteSearchRecyclerViewAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(new RouteSearchPredictionItem(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull RouteSearchRecyclerViewAdapter.ViewHolder holder, int i) {
        final int position = i;

        holder.predictionView.clear();

        Prediction prediction = predictions.get(position);
        holder.predictionView.setPrediction(prediction);

        if (prediction.getRoute() != null &&
                prediction.getRoute().getMode() == Route.COMMUTER_RAIL &&
                prediction.getTripName() != null &&
                !prediction.getTripName().equalsIgnoreCase("null")) {
            holder.predictionView.setTrainNumber(prediction.getTripName());

        } else if (prediction.getVehicle() != null &&
                prediction.getVehicle().getLabel() != null &&
                !prediction.getVehicle().getLabel().equalsIgnoreCase("null")) {
            if (prediction.getRoute().getMode() == Route.LIGHT_RAIL ||
                    prediction.getRoute().getMode() == Route.HEAVY_RAIL) {
                holder.predictionView.setTrainNumber(prediction.getVehicle().getLabel());
            } else {
                holder.predictionView.setVehicleNumber(prediction.getVehicle().getLabel());
            }

        } else if (prediction.getVehicleId() != null &&
                prediction.getVehicleId().equalsIgnoreCase("null")) {
            holder.predictionView.setVehicleNumber(prediction.getVehicleId());
        }

        if (position == predictions.size() - 1) {
            holder.predictionView.setBottomBorderVisible();
        }

        if (onItemClickListener != null) {
            holder.predictionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(position);
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

        for (Prediction p : predictions) {
            if (p.getPredictionTime() != null &&
                    (p.getVehicle() == null ||
                            !p.getVehicle().getTripId().equalsIgnoreCase(p.getTripId()) ||
                            p.getVehicle().getCurrentStopSequence() <= p.getStopSequence())) {
                this.predictions.add(p);
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        RouteSearchPredictionItem predictionView;

        ViewHolder(RouteSearchPredictionItem v) {
            super(v);
            predictionView = v;
        }
    }
}
