package jackwtat.simplembta.adapters;

import android.graphics.Color;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Direction;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.views.MapSearchPredictionItem;
import jackwtat.simplembta.views.PredictionHeaderView;

public class MapSearchRecyclerViewAdapter
        extends RecyclerView.Adapter<MapSearchRecyclerViewAdapter.ViewHolder> {
    public static final String LOG_TAG = "MSRVAdapter";

    private ArrayList<AdapterItem> adapterItems = new ArrayList<>();

    private Stop selectedStop;
    private Location targetLocation;
    private OnItemClickListener onHeaderClickListener;
    private OnItemClickListener onRouteClickListener;
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
        PredictionHeaderView stopHeader = predictionItem.findViewById(R.id.prediction_header);
        View routeHeader = predictionItem.findViewById(R.id.route_header_layout);
        View body = predictionItem.findViewById(R.id.predictions_layout);
        View bottomBorder = predictionItem.findViewById(R.id.bottom_border);

        Route thisRoute = adapterItems.get(i).route;
        Stop thisStop = adapterItems.get(i).stop;
        int thisDirection = adapterItems.get(i).direction;
        Stop previousStop = null;
        Stop nextStop = null;

        if (i > 0) {
            Route previousRoute = adapterItems.get(i - 1).route;
            int previousDirection = adapterItems.get(i - 1).direction;

            previousStop = previousRoute.getFocusStop(previousDirection);
        }

        if (i + 1 < adapterItems.size()) {
            nextStop = adapterItems.get(i + 1).getStop();
        }

        predictionItem.clear();
        predictionItem.setPredictions(thisRoute, thisDirection);

        stopHeader.reset();

        // Set prediction group header
        if ((thisStop != null && i == 0) ||
                (thisStop != null && previousStop != null && !thisStop.equals(previousStop))) {
            // Set the header text as the stop name
            stopHeader.setText(thisStop.getName());

            // Prevent most occurrences of scroll bug
            stopHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            stopHeader.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });

            // Add the colors
            Collections.sort(adapterItems.get(i).getStop().getRoutes());
            HashMap<String, Void> colors = new HashMap<>();

            for (Route route : adapterItems.get(i).getStop().getRoutes()) {
                int mode = route.getMode();
                String color = route.getPrimaryColor();

                if (!colors.containsKey(color) &&
                        (mode == Route.LIGHT_RAIL ||
                                mode == Route.HEAVY_RAIL)) {
                    stopHeader.addSecondaryColor(Color.parseColor(color));
                    colors.put(color, null);
                }
            }

            if (colors.size() == 0) {
                stopHeader.addSecondaryColor(Color.parseColor(thisRoute.getPrimaryColor()));
            }

            // Enable wheelchair accessibility icon
            stopHeader.setWheelchairAccessible(thisStop.isWheelchairAccessible());

            // Enable stop alert icon
            if (thisStop.getServiceAlerts().size() > 0) {
                boolean hasUrgentAlert = false;
                for (ServiceAlert alert : thisStop.getServiceAlerts()) {
                    if (alert.isUrgent()) {
                        hasUrgentAlert = true;
                        break;
                    }
                }
                stopHeader.enableStopAlertIcon(hasUrgentAlert);
            }

            // Set header OnClickListener
            stopHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onHeaderClickListener != null) {
                        onHeaderClickListener.onItemClick(i);
                    }
                }
            });

            stopHeader.setVisibility(View.VISIBLE);

        } else if (thisStop == null && (i == 0 || previousStop != null)) {
            stopHeader.setText(stopHeader.getContext().getResources().getString(R.string.no_predictions));
            stopHeader.setVisibility(View.VISIBLE);

            // Otherwise, hide the header
        } else {
            stopHeader.setVisibility(View.GONE);
        }

        if (i == adapterItems.size() - 1 ||
                (thisStop != null && nextStop == null) ||
                (thisStop != null && !thisStop.equals(nextStop))) {
            bottomBorder.setVisibility(View.VISIBLE);
        } else {
            bottomBorder.setVisibility(View.GONE);
        }

        routeHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onRouteClickListener != null) {
                    onRouteClickListener.onItemClick(i);
                }
            }
        });

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

    public AdapterItem getAdapterItem(int position) {
        return adapterItems.get(position);
    }

    public void setData(Location targetLocation, Route[] routes, Stop selectedStop) {
        adapterItems.clear();

        this.targetLocation = targetLocation;

        this.selectedStop = selectedStop;

        for (Route route : routes) {

            // Route has same stop for both directions
            if (route.hasPickUps(Direction.INBOUND) &&
                    route.hasPickUps(Direction.OUTBOUND) &&
                    route.getPredictions(Direction.INBOUND).get(0).getStop()
                            .equals(route.getPredictions(Direction.OUTBOUND).get(0).getStop())) {
                adapterItems.add(new AdapterItem(route, Direction.ALL_DIRECTIONS));

            } else {
                // Inbound predictions
                if (route.hasPickUps(Direction.INBOUND)) {
                    adapterItems.add(new AdapterItem(route, Direction.INBOUND));
                }

                // Outbound predictions
                if (route.hasPickUps(Direction.OUTBOUND)) {
                    adapterItems.add(new AdapterItem(route, Direction.OUTBOUND));
                }
            }

            // No predictions for either direction
            if (!route.hasPickUps(Direction.INBOUND) && !route.hasPickUps(Direction.OUTBOUND)) {
                Stop inboundStop = route.getFocusStop(Direction.INBOUND);
                Stop outboundStop = route.getFocusStop(Direction.OUTBOUND);

                if (inboundStop != null && outboundStop != null) {
                    if (inboundStop.getLocation().distanceTo(targetLocation) <
                            outboundStop.getLocation().distanceTo(targetLocation)) {
                        adapterItems.add(new AdapterItem(route, Direction.INBOUND));
                    } else {
                        adapterItems.add(new AdapterItem(route, Direction.OUTBOUND));
                    }
                } else {
                    if (inboundStop != null) {
                        adapterItems.add(new AdapterItem(route, Direction.INBOUND));

                    } else if (outboundStop != null) {
                        adapterItems.add(new AdapterItem(route, Direction.OUTBOUND));

                    } else {
                        adapterItems.add(new AdapterItem(route, Direction.NULL_DIRECTION));

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

    public void setOnHeaderClickListener(MapSearchRecyclerViewAdapter.OnItemClickListener listener) {
        if (listener != null) {
            this.onHeaderClickListener = listener;
        }
    }

    public void setOnRouteClickListener(MapSearchRecyclerViewAdapter.OnItemClickListener listener) {
        if (listener != null) {
            this.onRouteClickListener = listener;
        }
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
            this.stop = route.getFocusStop(direction);
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
            Stop thisStop = route.getFocusStop(direction);
            Stop otherStop = otherAdapterItem.route.getFocusStop(otherAdapterItem.direction);

            if (thisStop == null && otherStop == null) {
                return route.compareTo(otherAdapterItem.route);

            } else if (thisStop == null) {
                return 1;

            } else if (otherStop == null) {
                return -1;

            } else if (!thisStop.equals(otherStop)) {
                if (thisStop.equals(selectedStop)) {
                    return -1;

                } else if (otherStop.equals(selectedStop)) {
                    return 1;

                } else {
                    return Float.compare(thisStop.getLocation().distanceTo(targetLocation),
                            otherStop.getLocation().distanceTo(targetLocation));
                }

            } else if ((route.hasPickUps(direction) || direction == Direction.ALL_DIRECTIONS) &&
                    !otherAdapterItem.route.hasPickUps(otherAdapterItem.direction) &&
                    otherAdapterItem.direction != Direction.ALL_DIRECTIONS) {
                return -1;

            } else if (!route.hasPickUps(direction) && direction != Direction.ALL_DIRECTIONS &&
                    (otherAdapterItem.route.hasPickUps(otherAdapterItem.direction) ||
                            otherAdapterItem.direction == Direction.ALL_DIRECTIONS)) {
                return 1;

            } else if (!route.equals(otherAdapterItem.route)) {
                return route.compareTo(otherAdapterItem.route);

            } else {
                return otherAdapterItem.direction - direction;
            }
        }
    }
}
