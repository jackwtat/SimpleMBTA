package jackwtat.simplembta.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Direction;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Route;

public class MapSearchPredictionItem extends LinearLayout {
    View rootView;
    LinearLayout headerLayout;
    RelativeLayout bodyLayout;
    RouteNameView routeNameView;
    LinearLayout inboundListLayout;
    LinearLayout outboundListLayout;
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
        ArrayList<Prediction> predictions = new ArrayList<>();
        if (direction == Direction.ALL_DIRECTIONS) {
            predictions.addAll(route.getPredictions(0));
            predictions.addAll(route.getPredictions(1));
            direction = 0;
        } else {
            predictions.addAll(route.getPredictions(direction));
        }

        ArrayList<Prediction> pickUps = new ArrayList<>();

        // Find only predictions that pick up passengers
        for (Prediction p : predictions) {
            if (p.getPredictionTime() != null && p.willPickUpPassengers() &&
                    (p.getVehicle() == null ||
                            !p.getVehicle().getTripId().equalsIgnoreCase(p.getTripId()) ||
                            p.getVehicle().getCurrentStopSequence() <= p.getStopSequence())) {
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

            // For non-bus, add first prediction for each destination
            if (route.getMode() != Route.BUS) {
                ArrayList<String> destinations = new ArrayList<>();
                for (int i = 0; i < pickUps.size(); i++) {
                    Prediction p = pickUps.get(i);

                    if (!destinations.contains(p.getDestination())) {
                        if(p.getDirection() == Direction.INBOUND){
                            inboundListLayout.addView(
                                    new IndividualPredictionItem(
                                            getContext(), p,
                                            i == 0 ||
                                                    (i > 0 && p.getDirection() != pickUps.get(i - 1).getDirection())));
                        } else {
                            outboundListLayout.addView(
                                    new IndividualPredictionItem(
                                            getContext(), p,
                                            i == 0 ||
                                                    (i > 0 && p.getDirection() != pickUps.get(i - 1).getDirection())));
                        }

                        destinations.add(p.getDestination());
                    }
                }

                // For buses, if there is at least one live prediction, only add live predictions
            } else {
                boolean hasLive = false;
                for (Prediction p : pickUps) {
                    if (p.isLive()) {
                        hasLive = true;
                        break;
                    }
                }
                ArrayList<String> destinations = new ArrayList<>();
                for (int i = 0; i < pickUps.size(); i++) {
                    Prediction p = pickUps.get(i);

                    if ((p.isLive() || !hasLive) &&
                            !destinations.contains(p.getDestination())) {
                        if(p.getDirection()==Direction.INBOUND) {
                            inboundListLayout.addView(
                                    new IndividualPredictionItem(
                                            getContext(), p,
                                            i == 0 ||
                                                    (i > 0 && p.getDirection() != pickUps.get(i - 1).getDirection())));
                        }else{
                            outboundListLayout.addView(
                                    new IndividualPredictionItem(
                                            getContext(), p,
                                            i == 0 ||
                                                    (i > 0 && p.getDirection() != pickUps.get(i - 1).getDirection())));
                        }
                        destinations.add(p.getDestination());
                    }
                }
            }

            // Display appropriate message if there are no predictions
        } else {
            noPredictionsView.setText(getContext().getResources().getString(R.string.no_departures));
            noPredictionsView.setVisibility(VISIBLE);
        }
    }

    public void clear() {
        inboundListLayout.removeAllViews();
        outboundListLayout.removeAllViews();
        noPredictionsView.setVisibility(GONE);
        serviceAlertIndicatorView.setVisibility(GONE);
        serviceAdvisoryIndicatorView.setVisibility(GONE);
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.item_map_search_prediction, this);
        headerLayout = rootView.findViewById(R.id.route_header_layout);
        bodyLayout = rootView.findViewById(R.id.predictions_layout);
        routeNameView = rootView.findViewById(R.id.route_name_view);
        inboundListLayout = rootView.findViewById(R.id.inbound_list_layout);
        outboundListLayout = rootView.findViewById(R.id.outbound_list_layout);
        noPredictionsView = rootView.findViewById(R.id.no_predictions_text_view);
        serviceAlertIndicatorView = rootView.findViewById(R.id.service_alert_image_view);
        serviceAdvisoryIndicatorView = rootView.findViewById(R.id.service_advisory_image_view);
    }
}
