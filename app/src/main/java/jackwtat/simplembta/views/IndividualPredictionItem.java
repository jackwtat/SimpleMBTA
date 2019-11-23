package jackwtat.simplembta.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Vehicle;
import jackwtat.simplembta.model.routes.Route;

public class IndividualPredictionItem extends LinearLayout {
    View rootView;
    TextView destinationTextView;
    TextView trainNumberTextView;
    TextView trackNumberTextView;
    TextView timeTextView;
    TextView minuteTextView;

    String min;

    public IndividualPredictionItem(Context context) {
        super(context);
        init(context);
    }

    public IndividualPredictionItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IndividualPredictionItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public IndividualPredictionItem(Context context, Prediction prediction) {
        super(context);
        init(context);
        setPrediction(prediction);
    }

    public void setPrediction(Prediction prediction) {
        // Departure time
        long countdownTime = prediction.getCountdownTime();
        Vehicle vehicle = prediction.getVehicle();

        // There is a vehicle currently on this trip
        if (vehicle != null && vehicle.getTripId().equalsIgnoreCase(prediction.getTripId())) {
            // Vehicle has already passed this stop
            if (vehicle.getCurrentStopSequence() > prediction.getStopSequence()) {
                String statusText;
                if (prediction.getPredictionType() == Prediction.DEPARTURE) {
                    statusText = getContext().getResources().getString(R.string.individual_departed);
                } else {
                    statusText = getContext().getResources().getString(R.string.individual_arrived);
                }

                timeTextView.setText(statusText);
                minuteTextView.setVisibility(GONE);

                // Vehicle is at or approaching this stop
            } else if (vehicle.getCurrentStopSequence() == prediction.getStopSequence() ||
                    countdownTime < 30000) {
                // Vehicle is more than one minute away
                if (countdownTime > 60000) {
                    String timeText;
                    String minuteText;

                    if (countdownTime < 3600000) {
                        timeText = (countdownTime / 60000) + "";
                        minuteText = min;

                    } else {
                        Date predictionTime = prediction.getPredictionTime();
                        timeText = new SimpleDateFormat("h:mm").format(predictionTime);
                        minuteText = new SimpleDateFormat("a").format(predictionTime).toLowerCase();
                    }

                    timeTextView.setText(timeText);
                    minuteTextView.setText(minuteText);
                    minuteTextView.setVisibility(VISIBLE);

                    // Vehicle is less than one minute away
                } else {
                    String statusText;
                    if (vehicle.getCurrentStopSequence() == prediction.getStopSequence() &&
                            prediction.getPredictionType() == Prediction.DEPARTURE) {
                        statusText = getContext().getResources().getString(R.string.individual_departing);
                    } else {
                        statusText = getContext().getResources().getString(R.string.individual_arriving);
                    }

                    timeTextView.setText(statusText);
                    minuteTextView.setVisibility(GONE);
                }

                // Vehicle is not yet approaching this stop
            } else {
                String timeText;
                String minuteText;

                if (countdownTime < 3600000) {
                    timeText = (countdownTime / 60000) + "";
                    minuteText = min;

                } else {
                    Date predictionTime = prediction.getPredictionTime();
                    timeText = new SimpleDateFormat("h:mm").format(predictionTime);
                    minuteText = new SimpleDateFormat("a").format(predictionTime).toLowerCase();
                }

                timeTextView.setText(timeText);
                minuteTextView.setText(minuteText);
                minuteTextView.setVisibility(VISIBLE);
            }


            // No vehicle is on this trip
        } else {
            String timeText;
            String minuteText;

            if (countdownTime < 3600000) {
                if (countdownTime > 0) {
                    timeText = (countdownTime / 60000) + "";
                } else {
                    timeText = "0";
                }
                minuteText = min;

            } else {
                Date predictionTime = prediction.getPredictionTime();
                timeText = new SimpleDateFormat("h:mm").format(predictionTime);
                minuteText = new SimpleDateFormat("a").format(predictionTime).toLowerCase();
            }

            if(!prediction.isLive()) {
                minuteText += "*";
            }

            timeTextView.setText(timeText);
            minuteTextView.setText(minuteText);
            minuteTextView.setVisibility(VISIBLE);
        }

        int mode = prediction.getRoute().getMode();
        String tripName = prediction.getTripName();
        String trackNumber = prediction.getTrackNumber();

        // Destination
        destinationTextView.setText(prediction.getDestination());

        // Train number
        if (mode == Route.COMMUTER_RAIL &&
                tripName != null && !tripName.equalsIgnoreCase("null")) {
            tripName = getResources().getString(R.string.train) + " " + tripName;

            trainNumberTextView.setText(tripName);
            trainNumberTextView.setVisibility(VISIBLE);
        } else {
            trainNumberTextView.setVisibility(GONE);
        }

        // Track number
        if (mode == Route.COMMUTER_RAIL &&
                trackNumber != null && !trackNumber.equals("") && !trackNumber.equals("null")) {
            trackNumber = getContext().getResources().getString(R.string.track) + " " + trackNumber;
            trackNumberTextView.setText(trackNumber);
            trackNumberTextView.setVisibility(VISIBLE);
        } else {
            trackNumberTextView.setVisibility(GONE);
        }
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.item_individual_prediction, this);
        destinationTextView = rootView.findViewById(R.id.destination_text_view);
        trainNumberTextView = rootView.findViewById(R.id.vehicle_number_text_view);
        trackNumberTextView = rootView.findViewById(R.id.track_number_text_view);
        timeTextView = rootView.findViewById(R.id.time_text_view);
        minuteTextView = rootView.findViewById(R.id.minute_text_view);

        min = context.getResources().getString(R.string.min);
    }
}
