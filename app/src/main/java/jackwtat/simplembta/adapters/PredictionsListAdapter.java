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
import jackwtat.simplembta.mbta.structure.Mode;
import jackwtat.simplembta.mbta.structure.Prediction;
import jackwtat.simplembta.mbta.structure.Route;
import jackwtat.simplembta.mbta.structure.ServiceAlert;

/**
 * Created by jackw on 12/26/2017.
 */

public class PredictionsListAdapter extends ArrayAdapter<ArrayList<Prediction>> {
    public PredictionsListAdapter(
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
        TextView departureTime_4 = listItemView.findViewById(R.id.time_text_view_4);
        TextView trainNumber_1 = listItemView.findViewById(R.id.train_text_view_1);
        TextView trainNumber_2 = listItemView.findViewById(R.id.train_text_view_2);

        // Hide the views that have optional values for now
        alertIndicator.setVisibility(View.GONE);
        destination_2.setVisibility(View.GONE);
        departureTime_3.setVisibility(View.GONE);
        departureTime_4.setVisibility(View.GONE);
        trainNumber_1.setVisibility(View.GONE);
        trainNumber_2.setVisibility(View.GONE);

        // Set the stop name
        stopName.setText(p1.getStopName());

        // Set the route name
        setRouteView(p1.getRoute(), routeName);

        // Set the indicator for service alerts
        for (ServiceAlert alert : p1.getRoute().getServiceAlerts()) {
            alertIndicator.setVisibility(View.VISIBLE);
            if (alert.isActive() && (alert.getLifecycle() == ServiceAlert.Lifecycle.NEW || alert.getLifecycle() == ServiceAlert.Lifecycle.UNKNOWN)) {
                alertIndicator.setText(getContext().getResources().getString(R.string.service_alert_urgent));
                alertIndicator.setTextColor(ContextCompat.getColor(getContext(), R.color.ServiceAlert_Urgent));
                break;
            } else {
                alertIndicator.setText(getContext().getResources().getString(R.string.service_alert_advisory));
                alertIndicator.setTextColor(ContextCompat.getColor(getContext(), R.color.ServiceAlert_Advisory));
            }
        }

        // Set the destination and departure times of the predictions
        if (predictions.size() > 1 && p1.getDepartureTime() != null) {
            Prediction p2 = predictions.get(1);

            if (p2.getTrip().getDestination().equals(p1.getTrip().getDestination()) &&
                    p1.getRoute().getMode() != Mode.COMMUTER_RAIL) {
                setPredictionViews(p1, p2, departureTime_1, departureTime_2, destination_1);
            } else {
                setPredictionViews(p1, null, departureTime_1, departureTime_2, destination_1);
                setPredictionViews(p2, null, departureTime_3, departureTime_4, destination_2);
            }
        } else {
            setPredictionViews(p1, null, departureTime_1, departureTime_2, destination_1);
        }

        // Set the train numbers if commuter rail
        if (p1.getRoute().getMode() == Mode.COMMUTER_RAIL && !p1.getTrip().getName().equals("")) {
            String tno = "(Train " + p1.getTrip().getName() + ")";
            trainNumber_1.setText(tno);
            trainNumber_1.setVisibility(View.VISIBLE);

            if (predictions.size() > 1) {
                Prediction p2 = predictions.get(1);
                if (p2 != null && !p2.getTrip().getName().equals("")) {
                    tno = "(Train " + p2.getTrip().getName() + ")";
                    trainNumber_2.setText(tno);
                    trainNumber_2.setVisibility(View.VISIBLE);
                }
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
                routeView.setText(getContext().getResources().getString(R.string.red_line_short_name));
            else if (routeId.equals("Orange"))
                routeView.setText(getContext().getResources().getString(R.string.orange_line_short_name));
            else if (routeId.equals("Blue"))
                routeView.setText(getContext().getResources().getString(R.string.blue_line_short_name));
            else
                routeView.setText(routeId);

        } else if (mode == Mode.LIGHT_RAIL) {
            if (routeId.equals("Green-B"))
                routeView.setText(getContext().getResources().getString(R.string.green_line_b_short_name));
            else if (routeId.equals("Green-C"))
                routeView.setText(getContext().getResources().getString(R.string.green_line_c_short_name));
            else if (routeId.equals("Green-D"))
                routeView.setText(getContext().getResources().getString(R.string.green_line_d_short_name));
            else if (routeId.equals("Green-E"))
                routeView.setText(getContext().getResources().getString(R.string.green_line_e_short_name));
            else if (routeId.equals("Mattapan"))
                routeView.setText(getContext().getResources().getString(R.string.red_line_mattapan_short_name));
            else
                routeView.setText(routeId);

        } else if (mode == Mode.BUS) {
            if (routeId.equals("746"))
                routeView.setText(getContext().getResources().getString(R.string.silver_line_waterfront_short_name));
            else if (!rte.getShortName().equals("") && !rte.getShortName().equals("null"))
                routeView.setText(rte.getShortName());
            else
                routeView.setText(routeId);

        } else if (mode == Mode.COMMUTER_RAIL) {
            routeView.setText(getContext().getResources().getString(R.string.commuter_rail_short_name));

        } else if (mode == Mode.FERRY) {
            routeView.setText(getContext().getResources().getString(R.string.ferry_short_name));

        } else {
            routeView.setText(routeId);
        }
    }

    private void setPredictionViews(Prediction prediction_1, @Nullable Prediction prediction_2,
                                    TextView departureTimeView_1, TextView departureTimeView_2,
                                    TextView destinationView) {
        departureTimeView_1.setVisibility(View.VISIBLE);
        departureTimeView_2.setVisibility(View.VISIBLE);
        String dept_1;
        String dept_2;
        String dest;

        if (prediction_1.getDepartureTime() != null) {
            if (prediction_1.getTimeUntilDeparture() > 0) {
                dept_1 = (prediction_1.getTimeUntilDeparture() / 60000) + "";
            } else {
                dept_1 = "0";
            }

            if (prediction_2 != null && prediction_2.getDepartureTime() != null) {
                dept_1 = dept_1 + ",";
                if (prediction_2.getTimeUntilDeparture() > 0) {
                    dept_2 = (prediction_2.getTimeUntilDeparture() / 60000) + " min";
                } else {
                    dept_2 = "0 min";
                }
            } else {
                dept_2 = "min";
            }

            departureTimeView_1.setText(dept_1);
            departureTimeView_2.setText(dept_2);

            dest = prediction_1.getTrip().getDestination();
            destinationView.setText(dest);
            destinationView.setVisibility(View.VISIBLE);

        } else {
            departureTimeView_1.setText("---");
            departureTimeView_2.setText("");

            if (destinationView != null) {
                destinationView.setText(prediction_1.getTrip().getDestination());
                destinationView.setVisibility(View.VISIBLE);
            }
        }
    }
}
