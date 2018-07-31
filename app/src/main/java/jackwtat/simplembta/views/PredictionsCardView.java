package jackwtat.simplembta.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jackwtat.simplembta.R;
import jackwtat.simplembta.mbta.structure.Mode;
import jackwtat.simplembta.mbta.structure.Prediction;
import jackwtat.simplembta.mbta.structure.Route;
import jackwtat.simplembta.mbta.structure.ServiceAlert;

public class PredictionsCardView extends CardView {
    View rootView;
    LinearLayout routeLayout;
    LinearLayout predictionsLayout;
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

    public PredictionsCardView(Context context, List<Prediction> predictions) {
        super(context);
        init(context);
        setPredictions(predictions);
    }

    public void setPredictions(List<Prediction> predictions) {
        // Get the route
        Route route = predictions.get(0).getRoute();

        // Set the stop name
        stopNameView.setText(predictions.get(0).getStopName());

        // Set the route name
        routeLayout.addView(new RouteNameView(getContext(), route,
                getContext().getResources().getDimension(R.dimen.small_route_name_text_size),
                RouteNameView.ROUNDED_BACKGROUND,
                true, false));

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
    }

    public void clear() {
        routeLayout.removeAllViews();
        predictionsLayout.removeAllViews();
        stopNameView.setText("");
        serviceAlertIndicatorView.setVisibility(GONE);
        serviceAdvisoryIndicatorView.setVisibility(GONE);
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.predictions_card_view, this);

        routeLayout = rootView.findViewById(R.id.route_layout);
        predictionsLayout = rootView.findViewById(R.id.predictions_layout);
        stopNameView = rootView.findViewById(R.id.stop_text_view);
        serviceAlertIndicatorView = rootView.findViewById(R.id.service_alert_image_view);
        serviceAdvisoryIndicatorView = rootView.findViewById(R.id.service_advisory_image_view);

    }
}
