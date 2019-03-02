package jackwtat.simplembta.adapters;

import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Direction;
import jackwtat.simplembta.model.routes.Route;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.views.MapSearchPredictionItem;
import jackwtat.simplembta.views.PredictionHeaderView;

public class MapSearchRecyclerViewAdapter
        extends RecyclerView.Adapter<MapSearchRecyclerViewAdapter.ViewHolder> {
    public static final String LOG_TAG = "MSRVAdapter";

    private ArrayList<AdapterItem> adapterItems = new ArrayList<>();

    private Stop selectedStop;

    private OnItemClickListener onHeaderClickListener;
    private OnItemClickListener onBodyLongClickListener;

    private Location targetLocation;

    public MapSearchRecyclerViewAdapter() {
    }

    @NonNull
    @Override
    public MapSearchRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(new MapSearchPredictionItem(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull MapSearchRecyclerViewAdapter.ViewHolder holder, int position) {
        final int i = position;

        MapSearchPredictionItem predictionItem = holder.predictionView;
        PredictionHeaderView header = predictionItem.findViewById(R.id.prediction_header);

        Route thisRoute = adapterItems.get(i).route;
        Stop thisStop = adapterItems.get(i).stop;
        int thisDirection = adapterItems.get(i).direction;
        Stop previousStop = null;
        Stop nextStop = null;

        if (i > 0) {
            Route previousRoute = adapterItems.get(i - 1).route;
            int previousDirection = adapterItems.get(i - 1).direction;

            previousStop = previousRoute.getNearestStop(previousDirection);
        }

        if (i + 1 < adapterItems.size()) {
            nextStop = adapterItems.get(i + 1).getStop();
        }

        predictionItem.clear();
        predictionItem.setPredictions(thisRoute, thisDirection);

        header.reset();

        if ((thisStop != null && i == 0) ||
                (thisStop != null && previousStop != null && !thisStop.equals(previousStop))) {
            // Set the header text as the stop name
            header.setText(thisStop.getName());

            // Add the colors
            if (thisRoute.getMode() != Route.LIGHT_RAIL &&
                    thisRoute.getMode() != Route.HEAVY_RAIL)
                header.addSecondaryColor(Color.parseColor(thisRoute.getPrimaryColor()));
            else {
                HashMap<String, Void> colors = new HashMap<>();

                for (int j = i; j < adapterItems.size(); j++) {
                    AdapterItem item = adapterItems.get(j);
                    int mode = item.getRoute().getMode();
                    String color = item.getRoute().getPrimaryColor();

                    if (item.getStop() == null || !thisStop.equals(item.getStop())) {
                        break;
                    }

                    if (!colors.containsKey(color) &&
                            (mode == Route.LIGHT_RAIL || mode == Route.HEAVY_RAIL)) {
                        header.addSecondaryColor(Color.parseColor(color));
                        colors.put(color, null);
                    }
                }
            }

            header.setVisibility(View.VISIBLE);

        } else if (thisStop == null && (i == 0 || previousStop != null)) {
            header.setText(header.getContext().getResources().getString(R.string.no_predictions));
            header.setVisibility(View.VISIBLE);

            // Otherwise, hide the header
        } else {
            header.setVisibility(View.GONE);
        }

        holder.predictionView.setHeaderOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onHeaderClickListener != null)
                    onHeaderClickListener.onItemClick(i);
            }
        });
        holder.predictionView.setBodyOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onBodyLongClickListener != null)
                    onBodyLongClickListener.onItemClick(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return adapterItems.size();
    }

    public AdapterItem getAdapterItem(int position) {
        return adapterItems.get(position);
    }

    public void setData(Location targetLocation, Route[] routes, Stop selectedStop) {
        adapterItems.clear();

        this.targetLocation = targetLocation;

        this.selectedStop = selectedStop;

        for (Route route : routes) {
            boolean hasInboundPickUps = route.hasPickUps(Direction.INBOUND);
            boolean hasOutboundPickUps = route.hasPickUps(Direction.OUTBOUND);

            if (hasInboundPickUps || hasOutboundPickUps) {
                if (hasInboundPickUps) {
                    adapterItems.add(new AdapterItem(route, Direction.INBOUND));
                }
                if (hasOutboundPickUps) {
                    adapterItems.add(new AdapterItem(route, Direction.OUTBOUND));
                }
            } else if (route.hasNearbyStops()) {
                if (route.getNearestStop(Direction.INBOUND) != null) {
                    adapterItems.add(new AdapterItem(route, Direction.INBOUND));
                }
                if (route.getNearestStop(Direction.OUTBOUND) != null) {
                    adapterItems.add(new AdapterItem(route, Direction.OUTBOUND));
                }
            } else {
                adapterItems.add(new AdapterItem(route, Direction.NULL_DIRECTION));
            }
        }

        Collections.sort(adapterItems);

        notifyDataSetChanged();
    }

    public void clear() {
        adapterItems.clear();
        notifyDataSetChanged();
    }

    public void setOnHeaderClickListener(MapSearchRecyclerViewAdapter
                                                 .OnItemClickListener listener) {
        if (listener != null)
            this.onHeaderClickListener = listener;
    }

    public void setOnBodyClickListener(MapSearchRecyclerViewAdapter
                                               .OnItemClickListener listener) {
        if (listener != null)
            this.onBodyLongClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int i);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MapSearchPredictionItem predictionView;

        ViewHolder(MapSearchPredictionItem v) {
            super(v);
            predictionView = v;
        }
    }

    public class AdapterItem implements Comparable<AdapterItem> {
        Route route;
        Stop stop;
        int direction;

        AdapterItem(Route route, int direction) {
            this.route = route;
            this.stop = route.getNearestStop(direction);
            this.direction = direction;
        }


        public Route getRoute() {
            return route;
        }

        public Stop getStop() {
            return stop;
        }

        public int getDirection() {
            return direction;
        }

        @Override
        public int compareTo(@NonNull AdapterItem otherAdapterItem) {
            Stop thisStop = route.getNearestStop(direction);
            Stop otherStop = otherAdapterItem.route.getNearestStop(otherAdapterItem.direction);

            if (thisStop == null && otherStop == null) {
                return this.route.compareTo(otherAdapterItem.route);
            } else if (thisStop == null) {
                return 1;
            } else if (otherStop == null) {
                return -1;
            } else if (!thisStop.equals(otherStop)) {
                if (thisStop.equals(selectedStop)) {
                    return -1;
                } else if (otherStop.equals(selectedStop)) {
                    return 1;
                } else if (thisStop.getLocation().distanceTo(targetLocation) <
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
    }
}
