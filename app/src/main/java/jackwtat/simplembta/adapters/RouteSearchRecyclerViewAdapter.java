package jackwtat.simplembta.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.routes.Route;
import jackwtat.simplembta.views.RouteSearchPredictionItem;

public class RouteSearchRecyclerViewAdapter
        extends RecyclerView.Adapter<RouteSearchRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Prediction> predictions = new ArrayList<>();
    private OnItemClickListener onItemClickListener = null;
    private Route routeServiceAlerts = null;

    private boolean cleared = true;

    public RouteSearchRecyclerViewAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(new RouteSearchPredictionItem(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull RouteSearchRecyclerViewAdapter.ViewHolder holder, int i) {
        final int position = routeServiceAlerts == null ?
                i :
                i - 1;

        holder.predictionView.clear();

        if (position == -1) {
            holder.predictionView.setServiceAlerts(routeServiceAlerts);

        } else if (position == predictions.size()) {
            if (!cleared && predictions.size() == 0) {
                holder.predictionView.setNoPredictionsTextView(holder.predictionView.getContext().getResources().getString(R.string.no_departures));
            } else {
                holder.predictionView.setBottomBorderVisible();
            }

        } else {
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
                holder.predictionView.setVehicleNumber(prediction.getVehicle().getLabel());

            } else if (prediction.getVehicleId() != null &&
                    prediction.getVehicleId().equalsIgnoreCase("null")) {
                holder.predictionView.setVehicleNumber(prediction.getVehicleId());
            }
        }

        if (onItemClickListener != null) {
            holder.predictionView.enableOnClickAnimation(true);
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
        return routeServiceAlerts == null ?
                predictions.size() + 1 :
                predictions.size() + 2;
    }

    public Prediction getPrediction(int position) {
        return predictions.get(position);
    }

    public void setPredictions(List<Prediction> predictions) {
        this.predictions.clear();
        cleared = false;

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
        cleared = true;

        notifyDataSetChanged();
    }

    public void setServiceAlertsView(Route route) {
        routeServiceAlerts = route;
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
