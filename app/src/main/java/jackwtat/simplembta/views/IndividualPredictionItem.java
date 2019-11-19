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
        destinationTextView.setText(prediction.getDestination());

        // Set departure times
        String timeText;
        String minuteText;

        long countdownTime = prediction.getCountdownTime() + 15000;

        if (countdownTime <= 60 * 60000) {
            if (countdownTime > 0) {
                countdownTime /= 60000;
            }

            if (countdownTime > 0 || !prediction.isLive()) {
                timeText = countdownTime + "";
                minuteText = min;

                if (!prediction.isLive()) {
                    minuteText += "*";
                }

                timeTextView.setText(timeText);
                minuteTextView.setText(minuteText);
                minuteTextView.setVisibility(VISIBLE);
            } else {
                if (prediction.getPredictionType() == Prediction.DEPARTURE) {
                    timeText = getContext().getResources().getString(R.string.departing);
                } else {
                    timeText = getContext().getResources().getString(R.string.arriving);
                }

                timeTextView.setText(timeText);
                minuteTextView.setVisibility(GONE);
            }
        } else {
            Date predictionTime = prediction.getPredictionTime();
            timeText = new SimpleDateFormat("h:mm").format(predictionTime);
            minuteText = new SimpleDateFormat("a").format(predictionTime).toLowerCase();

            if (!prediction.isLive()) {
                minuteText += "*";
            }

            timeTextView.setText(timeText);
            minuteTextView.setText(minuteText);
            minuteTextView.setVisibility(VISIBLE);
        }

        int mode = prediction.getRoute().getMode();
        String tripName = prediction.getTripName();
        String trackNumber = prediction.getTrackNumber();

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
