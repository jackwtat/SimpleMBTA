package jackwtat.simplembta.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import jackwtat.simplembta.views.PredictionView;
import jackwtat.simplembta.R;
import jackwtat.simplembta.mbta.structure.Mode;
import jackwtat.simplembta.mbta.structure.Prediction;
import jackwtat.simplembta.mbta.structure.Route;
import jackwtat.simplembta.mbta.structure.ServiceAlert;
import jackwtat.simplembta.views.RouteNameView;

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
                    R.layout.item_prediction, parent, false);
        }

        // Check if there are no Predictions
        if (predictions == null || predictions.size() < 1) {
            return listItemView;
        }

        // Initialize all the views
        LinearLayout routeLayout = listItemView.findViewById(R.id.route_layout);
        LinearLayout predictionsLayout = listItemView.findViewById(R.id.predictions_layout);
        TextView stopNameView = listItemView.findViewById(R.id.stop_text_view);
        ImageView alertIndicatorView = listItemView.findViewById(R.id.service_alert_image_view);
        ImageView advisoryIndicatorView = listItemView.findViewById(R.id.service_advisory_image_view);

        // Hide the views that have optional values for now
        alertIndicatorView.setVisibility(View.GONE);
        advisoryIndicatorView.setVisibility(View.GONE);

        // Get the route
        Route route = predictions.get(0).getRoute();

        // Set the stop name
        stopNameView.setText(predictions.get(0).getStopName());

        // Set the route name
        if (routeLayout.getChildCount() > 0) {
            routeLayout.removeAllViews();
        }
        routeLayout.addView(new RouteNameView(getContext(), route,
                getContext().getResources().getDimension(R.dimen.small_route_name_text_size),
                RouteNameView.ROUNDED_BACKGROUND,
                true, false));

        // Set the indicator for service alerts
        for (ServiceAlert alert : route.getServiceAlerts()) {
            advisoryIndicatorView.setVisibility(View.VISIBLE);
            if (alert.isActive() && (alert.getLifecycle() == ServiceAlert.Lifecycle.NEW ||
                    alert.getLifecycle() == ServiceAlert.Lifecycle.UNKNOWN)) {
                alertIndicatorView.setVisibility(View.VISIBLE);
                advisoryIndicatorView.setVisibility(View.GONE);
                break;
            }
        }

        // Clear previous predictions
        if (predictionsLayout.getChildCount() > 0) {
            predictionsLayout.removeAllViews();
        }

        // Display the destinations and prediction times using PredictionViews
        if (route.getMode() != Mode.COMMUTER_RAIL) {
            // Group predictions by destination in a HashMap
            // Maintain the order of the destinations in an ArrayList
            HashMap<String, ArrayList<Prediction>> pMap = new HashMap<>();
            ArrayList<String> dList = new ArrayList<>();

            // Add the predictions to the HashMap and destinations to the ArrayList
            for (Prediction p : predictions) {
                String dest = p.getTrip().getDestination();
                if (!dList.contains(dest)) {
                    dList.add(dest);
                    pMap.put(dest, new ArrayList<Prediction>());
                }
                pMap.get(dest).add(p);
            }

            // Create a new PredictionView for each group of predictions
            for (String dest : dList) {
                ArrayList<Prediction> pList = pMap.get(dest);
                if (pList.size() > 1) {
                    predictionsLayout.addView(new PredictionView(getContext(), pList.get(0), pList.get(1)));
                } else {
                    predictionsLayout.addView(new PredictionView(getContext(), pList.get(0)));
                }
            }
        } else {
            // Create the PredictionViews for the Commuter Rail
            // Maximum 3 predictions to minimize scrolling
            for (int i = 0; i < 3 && i < predictions.size(); i++) {
                predictionsLayout.addView(new PredictionView(getContext(), predictions.get(i)));
            }
        }

        return listItemView;
    }
}
