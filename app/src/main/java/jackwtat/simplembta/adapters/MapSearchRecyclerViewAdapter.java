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
        View body = predictionItem.findViewById(R.id.predictions_card_body);
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

            // Prevent most occurrences of scroll bug
            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            header.setOnLongClickListener(new View.OnLongClickListener() {
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
                    header.addSecondaryColor(Color.parseColor(color));
                    colors.put(color, null);
                }
            }

            if (colors.size() == 0) {
                header.addSecondaryColor(Color.parseColor(thisRoute.getPrimaryColor()));
            }

            // Enable wheelchair accessibility icon
            header.setWheelchairAccessible(thisStop.isWheelchairAccessible());

            // Enable stop alert icon
            if (thisStop.getServiceAlerts().size() > 0) {
                boolean hasUrgentAlert = false;
                for (ServiceAlert alert : thisStop.getServiceAlerts()) {
                    if (alert.isUrgent()) {
                        hasUrgentAlert = true;
                        break;
                    }
                }
                header.enableStopAlertIcon(hasUrgentAlert);

                header.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(onHeaderClickListener!=null){
                            onHeaderClickListener.onItemClick(i);
                        }
                    }
                });
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
            // Display the inbound predictions
            if (route.hasPickUps(Direction.INBOUND)) {
                adapterItems.add(new AdapterItem(route, Direction.INBOUND));
            }

            // Display the outbound predictions
            if (route.hasPickUps(Direction.OUTBOUND)) {
                adapterItems.add(new AdapterItem(route, Direction.OUTBOUND));
            }

            // No predictions for either direction
            if (!route.hasPickUps(Direction.INBOUND) && !route.hasPickUps(Direction.OUTBOUND)) {
                Stop inboundStop = route.getNearestStop(Direction.INBOUND);
                Stop outboundStop = route.getNearestStop(Direction.OUTBOUND);

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

            } else if (route.hasPickUps(direction) &&
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
