package jackwtat.simplembta.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Direction;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.views.StopPredictionItem;


public class StopRecyclerViewAdapter
        extends RecyclerView.Adapter<StopRecyclerViewAdapter.ViewHolder>{
    public static final String LOG_TAG = "MSRVAdapter";

    private ArrayList<StopRecyclerViewAdapter.AdapterItem> adapterItems = new ArrayList<>();

    private StopRecyclerViewAdapter.OnItemClickListener onItemClickListener;
    private StopRecyclerViewAdapter.OnItemLongClickListener onItemLongClickListener;

    public StopRecyclerViewAdapter() {
    }

    @NonNull
    @Override
    public StopRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StopRecyclerViewAdapter.ViewHolder(new StopPredictionItem(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull StopRecyclerViewAdapter.ViewHolder holder, int position) {
        final int i = position;

        StopPredictionItem predictionItem = holder.predictionView;
        View body = predictionItem.findViewById(R.id.predictions_card_body);
        View bottomBorder = predictionItem.findViewById(R.id.bottom_border);

        Route thisRoute = adapterItems.get(i).route;
        int thisDirection = adapterItems.get(i).direction;

        predictionItem.clear();
        predictionItem.setPredictions(thisRoute, thisDirection);

        if (i == adapterItems.size() - 1) {
            bottomBorder.setVisibility(View.VISIBLE);
        } else {
            bottomBorder.setVisibility(View.GONE);
        }

        body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(i);
                }
            }
        });

        body.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (onItemLongClickListener != null) {
                    onItemLongClickListener.onItemLongClick(i);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return adapterItems.size();
    }

    public StopRecyclerViewAdapter.AdapterItem getAdapterItem(int position) {
        return adapterItems.get(position);
    }

    public void setData(Route[] routes) {
        adapterItems.clear();

        for (Route route : routes) {
            // Display the inbound predictions
            if (route.hasPickUps(Direction.INBOUND)) {
                adapterItems.add(new StopRecyclerViewAdapter.AdapterItem(route, Direction.INBOUND));
            }

            // Display the outbound predictions
            if (route.hasPickUps(Direction.OUTBOUND)) {
                adapterItems.add(new StopRecyclerViewAdapter.AdapterItem(route, Direction.OUTBOUND));
            }

            // No predictions for either direction
            if (!route.hasPickUps(Direction.INBOUND) && !route.hasPickUps(Direction.OUTBOUND)) {
                Stop inboundStop = route.getFocusStop(Direction.INBOUND);
                Stop outboundStop = route.getFocusStop(Direction.OUTBOUND);

                if (inboundStop != null && outboundStop != null) {
                    adapterItems.add(new StopRecyclerViewAdapter.AdapterItem(route, Direction.INBOUND));
                } else {
                    if (inboundStop != null) {
                        adapterItems.add(new StopRecyclerViewAdapter.AdapterItem(route, Direction.INBOUND));

                    } else if (outboundStop != null) {
                        adapterItems.add(new StopRecyclerViewAdapter.AdapterItem(route, Direction.OUTBOUND));

                    } else {
                        adapterItems.add(new StopRecyclerViewAdapter.AdapterItem(route, Direction.NULL_DIRECTION));

                    }
                }
            }
        }

        Collections.sort(adapterItems);

        notifyDataSetChanged();
    }

    public void clear() {
        adapterItems.clear();
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(StopRecyclerViewAdapter.OnItemClickListener listener) {
        if (listener != null) {
            this.onItemClickListener = listener;
        }
    }

    public void setOnItemLongClickListener(StopRecyclerViewAdapter.OnItemLongClickListener listener) {
        if (listener != null) {
            this.onItemLongClickListener = listener;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int i);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int i);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        StopPredictionItem predictionView;

        ViewHolder(StopPredictionItem v) {
            super(v);
            predictionView = v;
        }
    }

    public class AdapterItem implements Comparable<StopRecyclerViewAdapter.AdapterItem> {
        Route route;
        int direction;

        AdapterItem(Route route, int direction) {
            this.route = route;
            this.direction = direction;
        }

        public Route getRoute() {
            return route;
        }

        public int getDirection() {
            return direction;
        }

        @Override
        public int compareTo(@NonNull StopRecyclerViewAdapter.AdapterItem otherAdapterItem) {
            if (route.hasPickUps(direction) &&
                    !otherAdapterItem.route.hasPickUps(otherAdapterItem.direction)) {
                return -1;

            } else if (!route.hasPickUps(direction) &&
                    otherAdapterItem.route.hasPickUps(otherAdapterItem.direction)) {
                return 1;

            } else if (!route.equals(otherAdapterItem.route)) {
                return route.compareTo(otherAdapterItem.route);

            } else {
                return otherAdapterItem.direction - direction;
            }
        }
    }
}
