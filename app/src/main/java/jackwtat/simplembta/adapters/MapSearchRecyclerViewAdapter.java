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

    private OnItemClickListener onItemClickListener;

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
        int thisDirection = adapterItems.get(i).direction;
        Stop thisStop = thisRoute.getNearestStop(thisDirection);
        Stop previousStop = null;

        if (i > 0) {
            Route previousRoute = adapterItems.get(i - 1).route;
            int previousDirection = adapterItems.get(i - 1).direction;

            previousStop = previousRoute.getNearestStop(previousDirection);
        }

        predictionItem.clear();
        predictionItem.setPredictions(thisRoute, thisDirection);

        header.reset();

        // If this is the first prediction
        if (i == 0) {
            // If this is a selected prediction
            if (selectedStop != null && selectedStop.equals(thisStop)) {
                // Set the header text as the stop name
                header.setText(thisStop.getName());

                // Add the secondary colors
                HashMap<String, Void> colors = new HashMap<>();
                for (AdapterItem item : adapterItems) {
                    Route route = item.getRoute();
                    int mode = route.getMode();
                    Stop stop = route.getNearestStop(item.getDirection());
                    String color = route.getPrimaryColor();

                    if (stop == null || !stop.equals(selectedStop))
                        break;

                    if (!colors.containsKey(color) && stop.equals(selectedStop) &&
                            (mode == Route.LIGHT_RAIL || mode == Route.HEAVY_RAIL ||
                                    mode == Route.FERRY)) {
                        header.addSecondaryColor(Color.parseColor(color));
                        colors.put(color, null);
                    }
                }

                header.setVisibility(View.VISIBLE);

                // If this is not a selected prediction
            } else {
                header.setText(header.getContext().getResources().getString(R.string.nearby_services));

                header.setVisibility(View.VISIBLE);
            }

            // If this is the first non-selected prediction
        } else if (thisStop != null && !thisStop.equals(selectedStop) &&
                previousStop != null && previousStop.equals(selectedStop)) {
            header.setText(header.getContext().getResources().getString(
                    R.string.other_nearby_services));

            header.setVisibility(View.VISIBLE);

            // Otherwise, hide the header
        } else {
            header.setVisibility(View.GONE);
        }

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

    public void setOnItemClickListener(MapSearchRecyclerViewAdapter.OnItemClickListener onItemClickListener) {
        if (onItemClickListener != null)
            this.onItemClickListener = onItemClickListener;
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
        int direction;

        AdapterItem(Route route, int direction) {
            this.route = route;
            this.direction = direction;
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

        public Route getRoute() {
            return route;
        }

        public int getDirection() {
            return direction;
        }
    }
}
