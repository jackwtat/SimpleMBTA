package jackwtat.simplembta.views;

import android.content.Context;
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
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.utilities.Constants;

public class IndividualPredictionItem extends LinearLayout implements Constants {
    View rootView;
    PredictionTimeView predictionTimeView;
    TextView directionTextView;
    TextView destinationTextView;
    TextView trainNumberTextView;
    TextView trackNumberTextView;

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

    public IndividualPredictionItem(Context context, Prediction prediction, boolean showDirection) {
        super(context);
        init(context);
        setPrediction(prediction, showDirection);
    }

    public void setPrediction(Prediction prediction, boolean showDirection) {
        int mode = prediction.getRoute().getMode();
        String tripName = prediction.getTripName();
        String trackNumber = prediction.getTrackNumber();

        // Prediction time
        predictionTimeView.setPrediction(prediction);

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

        if (showDirection) {
            directionTextView.setText(prediction.getRoute().getDirection(prediction.getDirection()).getName());
            directionTextView.setVisibility(VISIBLE);
        } else {
            directionTextView.setVisibility(GONE);
        }
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.item_individual_prediction, this);
        predictionTimeView = rootView.findViewById(R.id.prediction_time_view);
        directionTextView = rootView.findViewById(R.id.direction_text_view);
        destinationTextView = rootView.findViewById(R.id.destination_text_view);
        trainNumberTextView = rootView.findViewById(R.id.vehicle_number_text_view);
        trackNumberTextView = rootView.findViewById(R.id.track_number_text_view);

        min = context.getResources().getString(R.string.min);
    }
}
