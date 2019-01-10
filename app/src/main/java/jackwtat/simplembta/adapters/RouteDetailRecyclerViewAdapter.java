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
import jackwtat.simplembta.views.RouteDetailPredictionItem;

public class RouteDetailRecyclerViewAdapter
        extends RecyclerView.Adapter<RouteDetailRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Prediction> predictions = new ArrayList<>();
    private OnItemClickListener onItemClickListener = null;
    private Route routeServiceAlerts = null;

    public RouteDetailRecyclerViewAdapter() {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(new RouteDetailPredictionItem(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull RouteDetailRecyclerViewAdapter.ViewHolder holder, int i) {
        final int position = routeServiceAlerts == null ?
                i :
                i - 1;

        holder.predictionView.clear();

        if (position == -1) {
            holder.predictionView.setServiceAlerts(routeServiceAlerts);

        } else if (position == predictions.size()) {
            if (predictions.size() == 0)
                holder.predictionView.setNoPredictionsTextView(holder.predictionView.getContext().getResources().getString(R.string.no_predictions_this_stop));
            else
                holder.predictionView.setNoPredictionsTextView(holder.predictionView.getContext().getResources().getString(R.string.no_further_predictions));

        } else {
            holder.predictionView.setPrediction(predictions.get(position));

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
    }

    @Override
    public int getItemCount() {
        return routeServiceAlerts == null ?
                predictions.size() + 1 :
                predictions.size() + 2;
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
        RouteDetailPredictionItem predictionView;

        ViewHolder(RouteDetailPredictionItem v) {
            super(v);
            predictionView = v;
        }
    }
}
