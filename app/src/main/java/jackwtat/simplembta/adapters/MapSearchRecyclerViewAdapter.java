package jackwtat.simplembta.adapters;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jackwtat.simplembta.model.Direction;
import jackwtat.simplembta.model.routes.Route;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.views.MapSearchPredictionItem;

public class MapSearchRecyclerViewAdapter
        extends RecyclerView.Adapter<MapSearchRecyclerViewAdapter.ViewHolder> {
    public static final String LOG_TAG = "MSRVAdapter";

    private ArrayList<adapterItem> adapterItems;

    private OnItemClickListener onItemClickListener;

    private Location targetLocation;

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
                if (thisStop.getLocation().distanceTo(targetLocation) <
                        otherStop.getLocation().distanceTo(targetLocation)) {
                    return -1;
                } else if (thisStop.getLocation().distanceTo(targetLocation) >
                        otherStop.getLocation().distanceTo(targetLocation)) {
                    return 1;
                } else {
                    return 0;
                }
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

    public void setData(Location targetLocation, List<Route> routes) {
        adapterItems.clear();

        this.targetLocation = targetLocation;

        for (Route route : routes) {
            boolean hasInboundPickUps = route.hasPickUps(Direction.INBOUND);
            boolean hasOutboundPickUps = route.hasPickUps(Direction.OUTBOUND);

            if (hasInboundPickUps || hasOutboundPickUps) {
                if (hasInboundPickUps) {
                    adapterItems.add(new adapterItem(route, Direction.INBOUND));
                }
                if (hasOutboundPickUps) {
                    adapterItems.add(new adapterItem(route, Direction.OUTBOUND));
                }
            } else if (route.hasNearbyStops()) {
                if (route.getNearestStop(Direction.INBOUND) != null) {
                    adapterItems.add(new adapterItem(route, Direction.INBOUND));
                }
                if (route.getNearestStop(Direction.OUTBOUND) != null) {
                    adapterItems.add(new adapterItem(route, Direction.OUTBOUND));
                }
            } else {
                adapterItems.add(new adapterItem(route, Direction.NULL_DIRECTION));
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
