package jackwtat.simplembta.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.routes.Route;
import jackwtat.simplembta.model.Stop;

public class MapSearchPredictionItem extends LinearLayout {
    View rootView;
    RouteNameView routeNameView;
    LinearLayout predictionsListLayout;
    TextView noPredictionsView;
    ImageView serviceAlertIndicatorView;
    ImageView serviceAdvisoryIndicatorView;

    public MapSearchPredictionItem(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MapSearchPredictionItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MapSearchPredictionItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setPredictions(Route route, int direction) {
        Stop stop = route.getNearestStop(direction);
        ArrayList<Prediction> predictions = route.getPredictions(direction);

        ArrayList<Prediction> pickUps = new ArrayList<>();

        // Find only predictions that pick up passengers
        for (Prediction p : predictions) {
            if (p.getPredictionTime() != null &&
                    p.willPickUpPassengers() &&
                    p.getCountdownTime() >= 0) {
                pickUps.add(p);
            }
        }

        Collections.sort(pickUps);

        // Set the indicator for service alerts
        if (route.getServiceAlerts().size() > 0) {
            if (route.hasUrgentServiceAlerts()) {
                serviceAlertIndicatorView.setVisibility(View.VISIBLE);
                serviceAdvisoryIndicatorView.setVisibility(View.GONE);
            } else {
                serviceAlertIndicatorView.setVisibility(View.GONE);
                serviceAdvisoryIndicatorView.setVisibility(View.VISIBLE);
            }
        }

        // Set the route name
        routeNameView.setRouteNameView(route);

        // Add predictions
        if (pickUps.size() > 0) {

            // For light rail and heavy rail, add first prediction for each destination
            if (route.getMode() == Route.LIGHT_RAIL || route.getMode() == Route.HEAVY_RAIL) {
                ArrayList<String> destinations = new ArrayList<>();
                for (Prediction p : pickUps) {
                    if (!destinations.contains(p.getDestination())) {
                        predictionsListLayout.addView(
                                new IndividualPredictionItem(getContext(), p));
                        destinations.add(p.getDestination());
                    }
                }

                // For buses, add the first live prediction
                // If there are no live predictions, then add the first non-live prediction
            } else if (route.getMode() == Route.BUS) {
                int i = 0;
                while (i < pickUps.size() && !pickUps.get(i).isLive()) {
                    i++;
                }

                if (i >= pickUps.size()) {
                    i = 0;
                }

                predictionsListLayout.addView(
                        new IndividualPredictionItem(getContext(), pickUps.get(i)));

                if (i + 1 < pickUps.size()) {
                    if (!pickUps.get(i).getDestination().equals(pickUps.get(i + 1).getDestination())) {
                        predictionsListLayout.addView(
                                new IndividualPredictionItem(getContext(), pickUps.get(i + 1)));
                    }
                }

                // For all other routes, add first prediction and second if it has a different
                // prediction
            } else {
                predictionsListLayout.addView(
                        new IndividualPredictionItem(getContext(), pickUps.get(0)));

                if (pickUps.size() > 1)
                    if (!pickUps.get(0).getDestination().equals(pickUps.get(1).getDestination()))
                        predictionsListLayout.addView(
                                new IndividualPredictionItem(getContext(), pickUps.get(1)));
            }

            // Display appropriate message if there are no predictions
        } else {
            StringBuilder stringBuilder = new StringBuilder();

            String directionText = route.getDirection(direction).getName().toLowerCase();
            if (!directionText.contains("bound"))
                directionText += "bound";

            if (stop != null) {
                stringBuilder.append(getContext().getResources().getString(R.string.no_predictions_this_stop_p1))
                        .append(" ")
                        .append(directionText)
                        .append(" ")
                        .append(getContext().getResources().getString(R.string.no_predictions_this_stop_p2));
            } else {
                stringBuilder.append(getContext().getResources().getString(R.string.no_nearby_predictions));
            }

            noPredictionsView.setText(stringBuilder.toString());
            noPredictionsView.setVisibility(VISIBLE);
        }
    }

    public void clear() {
        predictionsListLayout.removeAllViews();
        noPredictionsView.setVisibility(GONE);
        serviceAlertIndicatorView.setVisibility(GONE);
        serviceAdvisoryIndicatorView.setVisibility(GONE);
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.item_map_search_prediction, this);

        routeNameView = rootView.findViewById(R.id.route_name_view);
        predictionsListLayout = rootView.findViewById(R.id.predictions_list_layout);
        noPredictionsView = rootView.findViewById(R.id.no_predictions_text_view);
        serviceAlertIndicatorView = rootView.findViewById(R.id.service_alert_image_view);
        serviceAdvisoryIndicatorView = rootView.findViewById(R.id.service_advisory_image_view);
    }
}
