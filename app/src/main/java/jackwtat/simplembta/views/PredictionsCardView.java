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
import java.util.HashMap;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.model.ServiceAlert;

public class PredictionsCardView extends LinearLayout {
    View rootView;
    LinearLayout routeLayout;
    LinearLayout predictionsListLayout;
    TextView noPredictionsView;
    TextView stopNameView;
    ImageView serviceAlertIndicatorView;
    ImageView serviceAdvisoryIndicatorView;

    public PredictionsCardView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public PredictionsCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PredictionsCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setPredictions(Route route, Stop stop, Prediction[] predictions) {
        // Set the stop name
        if (stop != null) {
            stopNameView.setText(stop.getName());
            stopNameView.setVisibility(VISIBLE);
        }

        // Set the indicator for service alerts
        for (ServiceAlert alert : route.getServiceAlerts()) {
            serviceAdvisoryIndicatorView.setVisibility(View.VISIBLE);
            if (alert.isActive() && (alert.getLifecycle() == ServiceAlert.Lifecycle.NEW ||
                    alert.getLifecycle() == ServiceAlert.Lifecycle.UNKNOWN)) {
                serviceAlertIndicatorView.setVisibility(View.VISIBLE);
                serviceAdvisoryIndicatorView.setVisibility(View.GONE);
                break;
            }
        }

        // Set the route name
        boolean abbreviate;
        if (route.getMode() == Route.BUS) {
            abbreviate = true;
        } else {
            abbreviate = false;
        }
        routeLayout.addView(new RouteNameView(getContext(), route,
                getContext().getResources().getDimension(R.dimen.small_route_name_text_size),
                RouteNameView.ROUNDED_BACKGROUND,
                abbreviate, false));

        if (predictions.length > 0) {
            // Display the destinations and prediction times using PredictionViews
            // Group predictions by destination in a HashMap
            // Maintain the order of the destinations in an ArrayList
            HashMap<String, ArrayList<Prediction>> pMap = new HashMap<>();
            ArrayList<String> destList = new ArrayList<>();

            // Add the predictions to the HashMap and destinations to the ArrayList
            for (Prediction p : predictions) {
                String dest = p.getDestination();
                if (!destList.contains(dest)) {
                    destList.add(dest);
                    pMap.put(dest, new ArrayList<Prediction>());
                }
                pMap.get(dest).add(p);
            }

            // Create a new PredictionView for each group of predictions
            for (String dest : destList) {
                ArrayList<Prediction> pList = pMap.get(dest);
                predictionsListLayout.addView(new PredictionView(getContext(), pList.get(0)));
            }
        } else {
            if (stop != null) {
                noPredictionsView.setText(getContext().getResources().getString(R.string.no_current_predictions));
            } else {
                noPredictionsView.setText(getContext().getResources().getString(R.string.no_nearby_predictions));
            }
            noPredictionsView.setVisibility(VISIBLE);
        }
    }

    public void clear() {
        routeLayout.removeAllViews();
        predictionsListLayout.removeAllViews();
        noPredictionsView.setVisibility(GONE);
        stopNameView.setText("");
        stopNameView.setVisibility(GONE);
        serviceAlertIndicatorView.setVisibility(GONE);
        serviceAdvisoryIndicatorView.setVisibility(GONE);
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.predictions_card_view, this);

        routeLayout = rootView.findViewById(R.id.route_layout);
        predictionsListLayout = rootView.findViewById(R.id.predictions_list_layout);
        noPredictionsView = rootView.findViewById(R.id.no_predictions_text_view);
        stopNameView = rootView.findViewById(R.id.stop_text_view);
        serviceAlertIndicatorView = rootView.findViewById(R.id.service_alert_image_view);
        serviceAdvisoryIndicatorView = rootView.findViewById(R.id.service_advisory_image_view);

    }
}
