package jackwtat.simplembta.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Vehicle;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.utilities.Constants;

public class RouteSearchPredictionItem extends LinearLayout implements Constants {
    View rootView;
    View mainContent;
    PredictionTimeView predictionTimeView;
    TextView destinationTextView;
    TextView vehicleNumberTextView;
    ImageView enRouteIcon;
    View bottomEdge;
    View bottomBorder;

    ArrayList<View> indicators = new ArrayList<>();
    TextView spacer;
    TextView trackNumberIndicator;
    TextView enRouteIndicator;
    TextView notCrowdedIndicator;
    TextView someCrowdingIndicator;
    TextView veryCrowdedIndicator;
    TextView tomorrowIndicator;
    TextView weekDayIndicator;
    TextView cancelledIndicator;
    TextView dropOffIndicator;

    String min;

    public RouteSearchPredictionItem(Context context) {
        super(context);
        init(context);
    }

    public RouteSearchPredictionItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RouteSearchPredictionItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setPrediction(Prediction prediction) {
        setTime(prediction);
        setDestination(prediction.getDestination());
        setIndicators(prediction);

        mainContent.setVisibility(VISIBLE);
        bottomEdge.setVisibility(VISIBLE);
        bottomBorder.setVisibility(GONE);
    }

    public void setTime(Prediction prediction) {
        predictionTimeView.setPrediction(prediction);
    }

    public void setIndicators(Prediction prediction) {
        // Show indicators if prediction is for a future date
        setFutureIndicator(prediction);

        /*  1. If trip is cancelled, display cancelled indicator
            2. if stop is skipped, display skipped indicator
            3. If stop is drop-off only, display drop-off only indicator
            4. If route type is Commuter Rail and track number is available, display track number
            5. If vehicle is en route:
                a. If crowding data is available, display crowding indicator
                b. Otherwise, display en route indicator
            6. Otherwise, if future indicator is not displayed, display empty spacer */
        if (prediction.getStatus() == Prediction.CANCELLED) {
            cancelledIndicator.setText(R.string.cancelled);
            cancelledIndicator.setVisibility(VISIBLE);

        } else if (prediction.getStatus() == Prediction.SKIPPED) {
            cancelledIndicator.setText(R.string.skipped);
            cancelledIndicator.setVisibility(VISIBLE);

        } else if (!prediction.willPickUpPassengers()) {
            dropOffIndicator.setVisibility(VISIBLE);

        } else if (prediction.getRoute().getMode() == Route.COMMUTER_RAIL &&
                prediction.getTrackNumber() != null &&
                !prediction.getTrackNumber().equals("") &&
                !prediction.getTrackNumber().equals("null")) {
            String trackNumberText = getContext().getResources().getString(R.string.track) + " " +
                    prediction.getTrackNumber();
            trackNumberIndicator.setText(trackNumberText);
            trackNumberIndicator.setVisibility(VISIBLE);

        } else if (prediction.getVehicle() != null &&
                prediction.getVehicle().getTripId().equalsIgnoreCase(prediction.getTripId()) &&
                prediction.getVehicle().getCurrentStopSequence() > 1) {
            if (prediction.getVehicle().getPassengerLoad() != null &&
                prediction.getVehicle().getPassengerLoad() != Vehicle.PassengerLoad.UNKNOWN) {
                setPassengerLoadIndicator(prediction.getVehicle().getPassengerLoad());
            } else {
                enRouteIndicator.setVisibility(VISIBLE);
            }
        } else if (weekDayIndicator.getVisibility() == GONE &&
                tomorrowIndicator.getVisibility() == GONE) {
            spacer.setVisibility(VISIBLE);
        }
    }

    public void setFutureIndicator(Prediction prediction) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(prediction.getPredictionTime());
        int predictionDay = calendar.get(Calendar.DAY_OF_MONTH);
        int predictionWeekday = calendar.get(Calendar.DAY_OF_WEEK);
        String predictionDayName= calendar.getDisplayName(
                Calendar.DAY_OF_WEEK, Calendar.LONG,
                getResources().getConfiguration().getLocales().get(0));

        calendar.setTime(new Date());
        int todayDay = calendar.get(Calendar.DAY_OF_MONTH);
        int todayWeekday = calendar.get(Calendar.DAY_OF_WEEK);

        if (predictionDay - todayDay != 0) {
            if ((predictionWeekday - todayWeekday + 7) % 7 > 1) {
                weekDayIndicator.setText(predictionDayName.toUpperCase());
                weekDayIndicator.setVisibility(VISIBLE);
            } else {
                tomorrowIndicator.setVisibility(VISIBLE);
            }
        }
    }

    public void setPassengerLoadIndicator(Vehicle.PassengerLoad load) {
        switch (load) {
            case FULL:
                veryCrowdedIndicator.setVisibility(VISIBLE);
                break;
            case FEW_SEATS_AVAILABLE:
                someCrowdingIndicator.setVisibility(VISIBLE);
                break;
            case MANY_SEATS_AVAILABLE:
                notCrowdedIndicator.setVisibility(VISIBLE);
                break;
            default:
        }
    }

    public void setDestination(String destination) {
        destinationTextView.setText(destination);
    }

    public void setTrainNumber(String trainNumber) {
        String text = getResources().getString(R.string.train) +
                " " + trainNumber;

        vehicleNumberTextView.setText(text);
        vehicleNumberTextView.setVisibility(VISIBLE);
    }

    public void setVehicleNumber(String vehicleNumber) {
        if (vehicleNumber.substring(0, 1).equals("y")) {
            vehicleNumber = vehicleNumber.substring(1);
        }

        String text = getResources().getString(R.string.vehicle) +
                " " + vehicleNumber;

        vehicleNumberTextView.setText(text);
        vehicleNumberTextView.setVisibility(VISIBLE);
    }

    public void setBottomBorderVisible() {
        bottomBorder.setVisibility(VISIBLE);
    }

    public void clear() {
        predictionTimeView.clear();
        destinationTextView.setText("");
        vehicleNumberTextView.setText("");
        vehicleNumberTextView.setVisibility(GONE);

        for (View v : indicators) {
            v.setVisibility(GONE);
        }
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.item_route_search_prediction, this);
        mainContent = rootView.findViewById(R.id.predictions_card_body);
        predictionTimeView = rootView.findViewById(R.id.prediction_time_view);
        destinationTextView = rootView.findViewById(R.id.destination_text_view);
        vehicleNumberTextView = rootView.findViewById(R.id.vehicle_number_text_view);
        enRouteIcon = rootView.findViewById(R.id.enroute_icon);
        bottomEdge = rootView.findViewById(R.id.bottom_edge);
        bottomBorder = rootView.findViewById(R.id.bottom_border);

        spacer = rootView.findViewById(R.id.spacer_view);
        trackNumberIndicator = rootView.findViewById(R.id.track_number_text_view);
        enRouteIndicator = rootView.findViewById(R.id.en_route_text_view);
        notCrowdedIndicator = rootView.findViewById(R.id.not_crowded_text_view);
        someCrowdingIndicator = rootView.findViewById(R.id.some_crowding_text_view);
        veryCrowdedIndicator = rootView.findViewById(R.id.very_crowded_text_view);
        tomorrowIndicator = rootView.findViewById(R.id.tomorrow_text_view);
        weekDayIndicator = rootView.findViewById(R.id.week_day_text_view);
        cancelledIndicator = rootView.findViewById(R.id.cancelled_text_view);
        dropOffIndicator = rootView.findViewById(R.id.drop_off_text_view);

        indicators.add(spacer);
        indicators.add(trackNumberIndicator);
        indicators.add(enRouteIndicator);
        indicators.add(notCrowdedIndicator);
        indicators.add(someCrowdingIndicator);
        indicators.add(veryCrowdedIndicator);
        indicators.add(tomorrowIndicator);
        indicators.add(weekDayIndicator);
        indicators.add(cancelledIndicator);
        indicators.add(dropOffIndicator);

        min = context.getResources().getString(R.string.min);
    }
}
