package jackwtat.simplembta.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Prediction;

public class PredictionView extends LinearLayout {
    View rootView;

    TextView destinationTextView;
    TextView timeTextView;
    TextView minuteTextView;
    TextView trackNumberView;

    public PredictionView(Context context) {
        super(context);
    }

    public PredictionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PredictionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PredictionView(Context context, Prediction prediction) {
        super(context);
        init(context, prediction);
    }

    private void init(Context context, Prediction prediction) {
        rootView = inflate(context, R.layout.prediction_view, this);

        destinationTextView = rootView.findViewById(R.id.destination_text_view);
        timeTextView = rootView.findViewById(R.id.time_text_view);
        minuteTextView = rootView.findViewById(R.id.minute_text_view);
        trackNumberView = rootView.findViewById(R.id.track_number_text_view);

        destinationTextView.setText(prediction.getDestination());
/*
        // Set track number
        if (prediction.getRoute().getMode() == Route.COMMUTER_RAIL &&
                !prediction.getTrackNumber().equals("null")) {
            String trackNumber = context.getResources().getString(R.string.track) + " " +
                    prediction.getTrackNumber();
            trackNumberView.setText(trackNumber);

            Drawable background = context.getResources().getDrawable(R.drawable.rounded_background);
            DrawableCompat.setTint(background, context.getResources().getColor(R.color.ApproachingAlert));
            trackNumberView.setBackground(background);
        } else {
            trackNumberView.setVisibility(GONE);
        }
*/
        trackNumberView.setVisibility(GONE);

        // Set departure times
        if (prediction.willPickUpPassengers()) {
            long predictedTime;
            String timeText;

            if (prediction.getArrivalTime() != null) {
                predictedTime = prediction.getTimeUntilArrival();
            } else {
                predictedTime = prediction.getTimeUntilDeparture();
            }

            if (predictedTime > 0) {
                timeText = (predictedTime / 60000) + "";
            } else {
                timeText = "0";
            }

            timeTextView.setText(timeText);
        } else {
            timeTextView.setText("---");
            minuteTextView.setVisibility(GONE);
        }
    }
}
