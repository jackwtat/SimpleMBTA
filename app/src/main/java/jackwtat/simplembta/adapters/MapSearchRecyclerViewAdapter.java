package jackwtat.simplembta.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.views.MapSearchPredictionItem;

public class MapSearchRecyclerViewAdapter
        extends RecyclerView.Adapter<MapSearchRecyclerViewAdapter.ViewHolder> {
    public static final String LOG_TAG = "MSRVAdapter";

    private ArrayList<adapterItem> adapterItems;

    private OnItemClickListener onItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        MapSearchPredictionItem predictionView;

        ViewHolder(MapSearchPredictionItem v) {
            super(v);
            predictionView = v;
        }
    }

    public class adapterItem implements Comparable<adapterItem> {
        Route route;
        int direction;

        adapterItem(Route route, int direction) {
            this.route = route;
            this.direction = direction;
        }

        @Override
        public int compareTo(@NonNull adapterItem otherAdapterItem) {
            Stop thisStop = route.getNearestStop(direction);
            Stop otherStop = otherAdapterItem.route.getNearestStop(otherAdapterItem.direction);

            if (thisStop == null && otherStop == null) {
                return this.route.compareTo(otherAdapterItem.route);
            } else if (thisStop == null) {
                return 1;
            } else if (otherStop == null) {
                return -1;
            } else if (!thisStop.equals(otherStop)) {
                return thisStop.compareTo(otherStop);
            } else if (!this.route.equals(otherAdapterItem.route)) {
                return this.route.compareTo(otherAdapterItem.route);
            } else {
                return otherAdapterItem.direction - this.direction;
            }
        }

        public Route getRoute() {
            return route;
        }

        public int getDirection() {
            return direction;
        }
    }

    public MapSearchRecyclerViewAdapter() {
        adapterItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public MapSearchRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(new MapSearchPredictionItem(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull MapSearchRecyclerViewAdapter.ViewHolder holder, int position) {
        final int i = position;

        holder.predictionView.clear();
        holder.predictionView.setPredictions(
                adapterItems.get(i).route,
                adapterItems.get(i).getDirection());

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
        return adapterItems.size();
    }

    public adapterItem getAdapterItem(int position) {
        return adapterItems.get(position);
    }

    public void setRoutes(List<Route> routes) {
        adapterItems.clear();

        for (Route route : routes) {
            if (route.hasPickUps(Route.INBOUND) || route.hasPickUps(Route.OUTBOUND)) {
                if (route.hasPickUps(Route.INBOUND)) {
                    adapterItems.add(new adapterItem(route, Route.INBOUND));
                }
                if (route.hasPickUps(Route.OUTBOUND)) {
                    adapterItems.add(new adapterItem(route, Route.OUTBOUND));
                }
            } else if (route.hasNearbyStops()) {
                Stop inboundStop = route.getNearestStop(Route.INBOUND);
                Stop outboundStop = route.getNearestStop(Route.OUTBOUND);

                if (inboundStop != null && inboundStop.equals(outboundStop)) {
                    adapterItems.add(new adapterItem(route, Route.INBOUND));
                } else {
                    if (route.getNearestStop(Route.INBOUND) != null) {
                        adapterItems.add(new adapterItem(route, Route.INBOUND));
                    }
                    if (route.getNearestStop(Route.OUTBOUND) != null) {
                        adapterItems.add(new adapterItem(route, Route.OUTBOUND));
                    }
                }
            } else {
                adapterItems.add(new adapterItem(route, Route.NULL_DIRECTION));
            }
        }

        Collections.sort(adapterItems);

        notifyDataSetChanged();
    }

    public void clear() {
        adapterItems.clear();
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(MapSearchRecyclerViewAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int i);
    }
}
