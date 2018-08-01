package jackwtat.simplembta.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import jackwtat.simplembta.R;
import jackwtat.simplembta.mbta.structure.Prediction;
import jackwtat.simplembta.views.PredictionsCardView;

public class PredictionsAdapter extends RecyclerView.Adapter<PredictionsAdapter.ViewHolder> {
    private ArrayList<ArrayList<Prediction>> predictionGroups;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public PredictionsCardView predictionsCardView;

        public ViewHolder(PredictionsCardView v) {
            super(v);
            predictionsCardView = v;
        }
    }

    public PredictionsAdapter(){
        predictionGroups = new ArrayList<>();
    }

    public PredictionsAdapter(ArrayList<ArrayList<Prediction>> predictionGroups){
        this.predictionGroups = predictionGroups;
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
    }

    @Override
    public int getItemCount() {
        return predictionGroups.size();
    }

    public void addAll(ArrayList<ArrayList<Prediction>> predictionGroups){
        this.predictionGroups.clear();
        this.predictionGroups.addAll(predictionGroups);
        notifyDataSetChanged();
    }
}
