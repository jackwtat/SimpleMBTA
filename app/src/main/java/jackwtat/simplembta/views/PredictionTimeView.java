package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Vehicle;
import jackwtat.simplembta.utilities.Constants;

public class PredictionTimeView extends LinearLayout implements Constants {
    View rootView;
    TextView timeTextView;
    TextView periodTextView;

    String min = "";

    public PredictionTimeView(Context context) {
        super(context);
        init(context);
    }

    public PredictionTimeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PredictionTimeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setPrediction(Prediction prediction) {
        long countdownTime = prediction.getCountdownTime();
        Vehicle vehicle = prediction.getVehicle();

        // There is a vehicle currently on this trip and trip is not skipped or cancelled
        if (vehicle != null && vehicle.getTripId().equalsIgnoreCase(prediction.getTripId()) &&
                prediction.getStatus() != Prediction.SKIPPED &&
                prediction.getStatus() != Prediction.CANCELLED) {

            // Vehicle has already passed this stop
            if (vehicle.getCurrentStopSequence() > prediction.getStopSequence()) {
                String statusText;
                if (prediction.getPredictionType() == Prediction.DEPARTURE) {
                    statusText = getContext().getResources().getString(R.string.route_departed);
                } else {
                    statusText = getContext().getResources().getString(R.string.route_arrived);
                }

                timeTextView.setText(statusText);
                periodTextView.setVisibility(GONE);

                // Vehicle is at or approaching this stop
            } else if (vehicle.getCurrentStopSequence() == prediction.getStopSequence() ||
                    countdownTime < COUNTDOWN_APPROACHING_CUTOFF) {

                // Vehicle is more than one minute away
                if (countdownTime > COUNTDOWN_APPROACHING_CUTOFF) {
                    String timeText;
                    String minuteText;

                    if (countdownTime < COUNTDOWN_HOUR_CUTOFF) {
                        timeText = (countdownTime / 60000) + "";
                        minuteText = min;

                    } else {
                        Date predictionTime = prediction.getPredictionTime();
                        timeText = new SimpleDateFormat("h:mm").format(predictionTime);
                        minuteText = new SimpleDateFormat("a").format(predictionTime).toLowerCase();
                    }

                    timeTextView.setText(timeText);
                    periodTextView.setText(minuteText);
                    periodTextView.setVisibility(VISIBLE);

                    // Vehicle is less than one minute away
                } else {
                    String statusText;

                    if (prediction.getPredictionType() == Prediction.DEPARTURE &&
                            vehicle.getCurrentStatus() == Vehicle.Status.STOPPED) {
                        statusText = getContext().getResources().getString(R.string.route_departing);

                    } else if (countdownTime < COUNTDOWN_ARRIVING_CUTOFF) {
                        statusText = getContext().getResources().getString(R.string.route_arriving);

                    } else {
                        statusText = getContext().getResources().getString(R.string.route_approaching);
                    }

                    timeTextView.setText(statusText);
                    periodTextView.setVisibility(GONE);
                }

                // Vehicle is not yet approaching this stop
            } else {
                String timeText;
                String minuteText;

                if (countdownTime < COUNTDOWN_HOUR_CUTOFF) {
                    timeText = (countdownTime / 60000) + "";
                    minuteText = min;

                } else {
                    Date predictionTime = prediction.getPredictionTime();
                    timeText = new SimpleDateFormat("h:mm").format(predictionTime);
                    minuteText = new SimpleDateFormat("a").format(predictionTime).toLowerCase();
                }

                timeTextView.setText(timeText);
                periodTextView.setText(minuteText);
                periodTextView.setVisibility(VISIBLE);
            }

            // No vehicle is on this trip or trip is skipped or cancelled
        } else {
            String timeText;
            String minuteText;

            if (countdownTime < COUNTDOWN_HOUR_CUTOFF &&
                    prediction.getStatus() != Prediction.SKIPPED &&
                    prediction.getStatus() != Prediction.CANCELLED) {
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

            if (!prediction.isLive()) {
                minuteText += "*";
            }

            if (prediction.getStatus() == Prediction.SKIPPED ||
                    prediction.getStatus() == Prediction.CANCELLED) {
                strikeThrough();
            }

            timeTextView.setText(timeText);
            periodTextView.setText(minuteText);
            periodTextView.setVisibility(VISIBLE);
        }
    }

    public void strikeThrough() {
        timeTextView.setPaintFlags(timeTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        periodTextView.setPaintFlags(periodTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    public void clear() {
        timeTextView.setText("");
        periodTextView.setText("");

        timeTextView.setPaintFlags(timeTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        periodTextView.setPaintFlags(periodTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.prediction_time_view, this);
        timeTextView = rootView.findViewById(R.id.time_text_view);
        periodTextView = rootView.findViewById(R.id.period_text_view);

        min = context.getResources().getString(R.string.min);
    }
}
