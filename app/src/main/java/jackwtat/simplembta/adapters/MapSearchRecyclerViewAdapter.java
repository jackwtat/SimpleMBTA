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
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.routes.Route;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.views.MapSearchPredictionItem;
import jackwtat.simplembta.views.PredictionHeaderView;

public class MapSearchRecyclerViewAdapter
        extends RecyclerView.Adapter<MapSearchRecyclerViewAdapter.ViewHolder> {
    public static final String LOG_TAG = "MSRVAdapter";

    private ArrayList<AdapterItem> adapterItems = new ArrayList<>();

    private Stop selectedStop;
    private Location targetLocation;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;


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
        View bottomBorder = predictionItem.findViewById(R.id.bottom_border);

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

        // Set prediction group header
        if ((thisStop != null && i == 0) ||
                (thisStop != null && previousStop != null && !thisStop.equals(previousStop))) {
            // Set the header text as the stop name
            header.setText(thisStop.getName());

            // Add the colors
            Collections.sort(adapterItems.get(i).getStop().getRoutes());
            HashMap<String, Void> colors = new HashMap<>();

            for (Route route : adapterItems.get(i).getStop().getRoutes()) {
                int mode = route.getMode();
                String color = route.getPrimaryColor();

                if (!colors.containsKey(color) &&
                        (mode == Route.LIGHT_RAIL ||
                                mode == Route.HEAVY_RAIL)) {
                    header.addSecondaryColor(Color.parseColor(color));
                    colors.put(color, null);
                }
            }

            if (colors.size() == 0) {
                header.addSecondaryColor(Color.parseColor(thisRoute.getPrimaryColor()));
            }

            header.setVisibility(View.VISIBLE);

        } else if (thisStop == null && (i == 0 || previousStop != null)) {
            header.setText(header.getContext().getResources().getString(R.string.no_predictions));
            header.setVisibility(View.VISIBLE);

            // Otherwise, hide the header
        } else {
            header.setVisibility(View.GONE);
        }

        if (i == adapterItems.size() - 1 ||
                (thisStop != null && nextStop == null) ||
                (thisStop != null && !thisStop.equals(nextStop))) {
            bottomBorder.setVisibility(View.VISIBLE);
        } else {
            bottomBorder.setVisibility(View.GONE);
        }

        holder.predictionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(i);
                }
            }
        });

        holder.predictionView.setOnLongClickListener(new View.OnLongClickListener() {
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

    public AdapterItem getAdapterItem(int position) {
        return adapterItems.get(position);
    }

    public void setData(Location targetLocation, Route[] routes, Stop selectedStop) {
        adapterItems.clear();

        this.targetLocation = targetLocation;

        this.selectedStop = selectedStop;

        for (Route route : routes) {
            if (route.getNearestStop(Direction.INBOUND) == null) {
                Stop[] inboundStops = route.getStops(Direction.INBOUND);

                for (Stop stop : inboundStops) {
                    Stop nearestStop = route.getNearestStop(Direction.INBOUND);
                    if (nearestStop == null
                            || stop.getLocation().distanceTo(targetLocation) <
                            nearestStop.getLocation().distanceTo(targetLocation)) {
                        route.setNearestStop(Direction.INBOUND, stop);
                    }
                }
            }

            if (route.getNearestStop(Direction.OUTBOUND) == null) {
                Stop[] outboundStops = route.getStops(Direction.OUTBOUND);

                for (Stop stop : outboundStops) {
                    Stop nearestStop = route.getNearestStop(Direction.OUTBOUND);
                    if (nearestStop == null
                            || stop.getLocation().distanceTo(targetLocation) <
                            nearestStop.getLocation().distanceTo(targetLocation)) {
                        route.setNearestStop(Direction.OUTBOUND, stop);
                    }
                }
            }

            if (route.getNearestStop(Direction.INBOUND) != null &&
                    route.getNearestStop(Direction.OUTBOUND) != null
                    && !route.getNearestStop(Direction.INBOUND)
                    .equals(route.getNearestStop(Direction.OUTBOUND))) {
                adapterItems.add(new AdapterItem(route, Direction.INBOUND));
                adapterItems.add(new AdapterItem(route, Direction.OUTBOUND));

            } else {
                if (route.hasPickUps(Direction.INBOUND)) {
                    adapterItems.add(new AdapterItem(route, Direction.INBOUND));
                }

                if (route.hasPickUps(Direction.OUTBOUND)) {
                    adapterItems.add(new AdapterItem(route, Direction.OUTBOUND));
                }

                if (!route.hasPickUps(Direction.INBOUND) && !route.hasPickUps(Direction.OUTBOUND)) {
                    adapterItems.add(new AdapterItem(route, Direction.NULL_DIRECTION));
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

    public void setOnItemClickListener(MapSearchRecyclerViewAdapter.OnItemClickListener listener) {
        if (listener != null) {
            this.onItemClickListener = listener;
        }
    }

    public void setOnItemLongClickListener(MapSearchRecyclerViewAdapter.OnItemLongClickListener listener) {
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
            Prediction[] thisPredictions = route.getPredictions(direction).toArray(new Prediction[0]);
            Prediction[] otherPredictions = otherAdapterItem.route.getPredictions(direction).toArray(new Prediction[0]);

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
            } else if ((thisPredictions == null || thisPredictions.length == 0) &&
                    (otherPredictions != null && otherPredictions.length > 0)) {
                return 1;
            } else if ((otherPredictions == null || otherPredictions.length == 0)
                    && (thisPredictions != null && thisPredictions.length > 0)) {
                return -1;
            } else if (!this.route.equals(otherAdapterItem.route)) {
                return this.route.compareTo(otherAdapterItem.route);
            } else {
                return otherAdapterItem.direction - this.direction;
            }
        }
    }
}
