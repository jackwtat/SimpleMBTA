package jackwtat.simplembta.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import jackwtat.simplembta.mbta.structure.Prediction;
import jackwtat.simplembta.mbta.structure.Route;
import jackwtat.simplembta.views.PredictionsCardView;

public class PredictionsAdapter extends RecyclerView.Adapter<PredictionsAdapter.ViewHolder> {
    private ArrayList<ArrayList<Prediction>> predictionGroups;

    private OnItemClickListener onItemClickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public PredictionsCardView predictionsCardView;

        public ViewHolder(PredictionsCardView v) {
            super(v);
            predictionsCardView = v;
        }
    }

    public PredictionsAdapter() {
        predictionGroups = new ArrayList<>();
    }

    @NonNull
    @Override
    public PredictionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PredictionsCardView v = new PredictionsCardView(parent.getContext());

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(PredictionsAdapter.ViewHolder holder, int position) {
        holder.predictionsCardView.clear();
        holder.predictionsCardView.setPredictions(predictionGroups.get(position));

        final int i = position;

        holder.predictionsCardView.setOnClickListener(new View.OnClickListener() {
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
        return predictionGroups.size();
    }

    public Route getRoute(int position) {
        return predictionGroups.get(position).get(0).getRoute();
    }

    public void setPredictions(ArrayList<ArrayList<Prediction>> predictionGroups) {
        this.predictionGroups.clear();
        this.predictionGroups.addAll(predictionGroups);
        notifyDataSetChanged();
    }

    public void clear(){
        predictionGroups.clear();
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int i);
    }
}
