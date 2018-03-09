package jackwtat.simplembta.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.graphics.Color;

import java.util.ArrayList;

import jackwtat.simplembta.R;
import jackwtat.simplembta.mbta.structures.Mode;
import jackwtat.simplembta.mbta.structures.Prediction;
import jackwtat.simplembta.mbta.structures.Route;
import jackwtat.simplembta.mbta.structures.ServiceAlert;

/**
 * Created by jackw on 12/26/2017.
 */

public class PredictionPairListAdapter extends ArrayAdapter<ArrayList<Prediction>> {
    public PredictionPairListAdapter(
            @NonNull Context context, ArrayList<ArrayList<Prediction>> predictions) {
        super(context, 0, predictions);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;

        ArrayList<Prediction> predictions = getItem(position);

        // Inflate the listItemView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.prediction_list_item, parent, false);
        }

        // Check if there are no Predictions
        if (predictions == null || predictions.size() < 1) {
            return listItemView;
        }

        // Get first and second prediction
        Prediction p1 = predictions.get(0);

        // Initialize all the views
        TextView routeName = listItemView.findViewById(R.id.route_text_view);
        TextView stopName = listItemView.findViewById(R.id.stop_text_view);
        TextView alertIndicator = listItemView.findViewById(R.id.alert_indicator_text_view);
        TextView destination_1 = listItemView.findViewById(R.id.destination_text_view_1);
        TextView destination_2 = listItemView.findViewById(R.id.destination_text_view_2);
        TextView departureTime_1 = listItemView.findViewById(R.id.time_text_view_1);
        TextView departureTime_2 = listItemView.findViewById(R.id.time_text_view_2);
        TextView departureTime_3 = listItemView.findViewById(R.id.time_text_view_3);

        // Hide the views that have optional values for now
        destination_2.setVisibility(View.GONE);
        departureTime_2.setVisibility(View.GONE);
        departureTime_3.setVisibility(View.GONE);
        alertIndicator.setVisibility(View.GONE);

        // Set the stop name
        stopName.setText(p1.getStopName());

        // Set the route name
        setRouteView(p1.getRoute(), routeName);

        // Set the destination and departure time for the first prediction
        setPredictionViews(p1, departureTime_1, destination_1);

        // Set the indicator for service alerts
        for (ServiceAlert alert : p1.getRoute().getServiceAlerts()) {
            alertIndicator.setVisibility(View.VISIBLE);
            if (alert.isActive() && (alert.getLifecycle() == ServiceAlert.Lifecycle.NEW || alert.getLifecycle() == ServiceAlert.Lifecycle.UNKNOWN)) {
                alertIndicator.setText(getContext().getResources().getString(R.string.service_alert_urgent));
                break;
            } else {
                alertIndicator.setText(getContext().getResources().getString(R.string.service_alert_advisory));
            }
        }

        // Set the departure time of the second prediction if it exists,
        // departure time of first prediction is not null (i.e. prediction is not an alerts holder)
        // and destination is different from first prediction
        if (predictions.size() > 1 && p1.getDepartureTime() != null) {
            Prediction p2 = predictions.get(1);

            if (p2.getTrip().getDestination()
                    .equals(p1.getTrip().getDestination())) {
                setPredictionViews(p2, departureTime_3, null);
            } else {
                setPredictionViews(p2, departureTime_2, destination_2);
            }
        }

        return listItemView;
    }

    private void setRouteView(Route rte, TextView routeView) {
        Drawable bkgd = getContext().getResources().getDrawable(R.drawable.route_background);
        DrawableCompat.setTint(bkgd, Color.parseColor(rte.getColor()));
        routeView.setBackground(bkgd);

        routeView.setTextColor(Color.parseColor(rte.getTextColor()));

        String routeId = rte.getId();
        Mode mode = rte.getMode();

        if (mode == Mode.HEAVY_RAIL) {
            if (routeId.equals("Red"))
                routeView.setText("RL");
            else if (routeId.equals("Orange"))
                routeView.setText("OL");
            else if (routeId.equals("Blue"))
                routeView.setText("BL");

        } else if (mode == Mode.LIGHT_RAIL) {
            if (routeId.equals("Green-B"))
                routeView.setText("GL-B");
            else if (routeId.equals("Green-C"))
                routeView.setText("GL-C");
            else if (routeId.equals("Green-D"))
                routeView.setText("GL-D");
            else if (routeId.equals("Green-E"))
                routeView.setText("GL-E");
            else if (routeId.equals("Mattapan"))
                routeView.setText("RL-M");

        } else if (mode == Mode.BUS) {
            if (routeId.equals("746"))
                routeView.setText("SL");
            else if (!rte.getShortName().equals("") && !rte.getShortName().equals("null"))
                routeView.setText(rte.getShortName());
            else
                routeView.setText(rte.getId());

        } else if (mode == Mode.COMMUTER_RAIL) {
            routeView.setText("CR");

        } else if (mode == Mode.FERRY) {
            routeView.setText("BOAT");

        } else {
            routeView.setText(rte.getId());
        }
    }

    private void setPredictionViews(Prediction prediction, TextView departureTimeView,
                                    @Nullable TextView destinationView) {
        departureTimeView.setVisibility(View.VISIBLE);

        if (prediction.getDepartureTime() != null) {
            if (prediction.timeUntilDeparture() > 0) {
                String dt = (prediction.timeUntilDeparture() / 60000) + " min";
                departureTimeView.setText(dt);
            } else {
                departureTimeView.setText("0 min");
            }


            if (destinationView != null) {
                destinationView.setText(prediction.getTrip().getDestination());
                destinationView.setVisibility(View.VISIBLE);

                if (prediction.timeUntilDeparture() / 60000 <= 5) {
                    departureTimeView.setBackgroundColor(ContextCompat.getColor(getContext(),
                            R.color.ApproachingAlert));
                    departureTimeView.setTextColor(ContextCompat.getColor(getContext(),
                            R.color.HighlightedText));
                } else {
                    departureTimeView.setBackgroundColor(ContextCompat.getColor(getContext(),
                            R.color.Transparent));
                    departureTimeView.setTextColor(ContextCompat.getColor(getContext(),
                            R.color.PrimaryText));
                }
            }
        } else {
            departureTimeView.setText("---");
            departureTimeView.setBackgroundColor(ContextCompat.getColor(getContext(),
                    R.color.Transparent));
            departureTimeView.setTextColor(ContextCompat.getColor(getContext(),
                    R.color.PrimaryText));

            if (destinationView != null) {
                destinationView.setText(prediction.getTrip().getDestination());
                destinationView.setVisibility(View.VISIBLE);
            }
        }
    }
}
