package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
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
    TextView liveIndicator;
    TextView trackNumberIndicator;
    TextView enRouteIndicator;
    TextView tomorrowIndicator;
    TextView weekDayIndicator;
    TextView cancelledIndicator;
    TextView dropOffIndicator;
    TextView destinationTextView;
    TextView vehicleNumberTextView;
    ImageView enRouteIcon;
    View bottomEdge;
    View bottomBorder;

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
        setFutureIndicator(prediction);
        setLiveIndicator(prediction);

        mainContent.setVisibility(VISIBLE);
        bottomEdge.setVisibility(VISIBLE);
        bottomBorder.setVisibility(GONE);
    }

    public void setTime(Prediction prediction) {
        predictionTimeView.setPrediction(prediction);
    }

    public void setFutureIndicator(Prediction prediction) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(prediction.getPredictionTime());
        int predictionDay = calendar.get(Calendar.DAY_OF_MONTH);
        int predictionWeekday = calendar.get(Calendar.DAY_OF_WEEK);
        String predictionDayName = calendar.getDisplayName(
                Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);

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

    public void setLiveIndicator(Prediction prediction) {
        // Show the appropriate status indicators
        if (prediction.getStatus() == Prediction.CANCELLED ||
                prediction.getStatus() == Prediction.SKIPPED) {

            if (prediction.getStatus() == Prediction.CANCELLED) {
                cancelledIndicator.setText(R.string.cancelled);
            } else {
                cancelledIndicator.setText(R.string.skipped);
            }

            cancelledIndicator.setVisibility(VISIBLE);
            liveIndicator.setVisibility(GONE);

        } else if (!prediction.willPickUpPassengers()) {
            dropOffIndicator.setVisibility(VISIBLE);
            liveIndicator.setVisibility(GONE);

        } else if (prediction.isLive()) {
            int mode = prediction.getRoute().getMode();
            String trackNumber = prediction.getTrackNumber();

            if (mode == Route.COMMUTER_RAIL &&
                    trackNumber != null && !trackNumber.equals("") && !trackNumber.equals("null")) {
                trackNumber = getContext().getResources().getString(R.string.track) + " " + trackNumber;
                trackNumberIndicator.setText(trackNumber);
                trackNumberIndicator.setVisibility(VISIBLE);
                liveIndicator.setVisibility(GONE);

            } else if (prediction.getVehicle() != null &&
                    prediction.getVehicle().getCurrentStopSequence() > 1 &&
                    prediction.getTripId().equalsIgnoreCase(prediction.getVehicle().getTripId())) {
                enRouteIndicator.setVisibility(VISIBLE);
                liveIndicator.setVisibility(GONE);

            } else {
                trackNumberIndicator.setVisibility(GONE);
                enRouteIndicator.setVisibility(GONE);
                liveIndicator.setVisibility(VISIBLE);
            }
        }

        if (liveIndicator.getVisibility() == GONE &&
                trackNumberIndicator.getVisibility() == GONE &&
                enRouteIndicator.getVisibility() == GONE &&
                cancelledIndicator.getVisibility() == GONE &&
                dropOffIndicator.getVisibility() == GONE &&
                tomorrowIndicator.getVisibility() == GONE &&
                weekDayIndicator.getVisibility() == GONE) {
            liveIndicator.setVisibility(INVISIBLE);
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
        liveIndicator.setVisibility(GONE);
        trackNumberIndicator.setVisibility(GONE);
        enRouteIndicator.setVisibility(GONE);
        tomorrowIndicator.setVisibility(GONE);
        weekDayIndicator.setVisibility(GONE);
        cancelledIndicator.setVisibility(GONE);
        dropOffIndicator.setVisibility(GONE);
        destinationTextView.setText("");
        vehicleNumberTextView.setText("");
        vehicleNumberTextView.setVisibility(GONE);
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.item_route_search_prediction, this);
        mainContent = rootView.findViewById(R.id.predictions_card_body);
        predictionTimeView = rootView.findViewById(R.id.prediction_time_view);
        liveIndicator = rootView.findViewById(R.id.live_text_view);
        trackNumberIndicator = rootView.findViewById(R.id.track_number_text_view);
        enRouteIndicator = rootView.findViewById(R.id.en_route_text_view);
        tomorrowIndicator = rootView.findViewById(R.id.tomorrow_text_view);
        weekDayIndicator = rootView.findViewById(R.id.week_day_text_view);
        cancelledIndicator = rootView.findViewById(R.id.cancelled_text_view);
        dropOffIndicator = rootView.findViewById(R.id.drop_off_text_view);
        destinationTextView = rootView.findViewById(R.id.destination_text_view);
        vehicleNumberTextView = rootView.findViewById(R.id.vehicle_number_text_view);
        enRouteIcon = rootView.findViewById(R.id.enroute_icon);
        bottomEdge = rootView.findViewById(R.id.bottom_edge);
        bottomBorder = rootView.findViewById(R.id.bottom_border);

        min = context.getResources().getString(R.string.min);
    }
}
