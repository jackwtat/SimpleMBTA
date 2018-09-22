package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Prediction;

public class PredictionView extends LinearLayout {
    View rootView;

    TextView destinationTextView;
    TextView timeTextView;
    TextView minuteTextView;
    TextView trackNumberTextView;
    TextView statusTextView;

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
        trackNumberTextView = rootView.findViewById(R.id.track_number_text_view);
        statusTextView = rootView.findViewById(R.id.status_text_view);

        destinationTextView.setText(prediction.getDestination());
/*
        // Set track number
        if (prediction.getRoute().getMode() == Route.COMMUTER_RAIL &&
                !prediction.getTrackNumber().equals("null")) {
            String trackNumber = context.getResources().getString(R.string.track) + " " +
                    prediction.getTrackNumber();
            trackNumberTextView.setText(trackNumber);

            Drawable background = context.getResources().getDrawable(R.drawable.rounded_background);
            DrawableCompat.setTint(background, context.getResources().getColor(R.color.ApproachingAlert));
            trackNumberTextView.setBackground(background);
        } else {
            trackNumberTextView.setVisibility(GONE);
        }
*/

        // Set departure times
        if (prediction.willPickUpPassengers()) {
            String timeText;
            String minuteText;

            Date predictionTime;
            long countdownTime;

            if (prediction.getArrivalTime() != null) {
                predictionTime = prediction.getArrivalTime();
                countdownTime = prediction.getTimeUntilArrival();
            } else {
                predictionTime = prediction.getDepartureTime();
                countdownTime = prediction.getTimeUntilDeparture();
            }

            if (countdownTime < 60 * 60000) {
                if (countdownTime > 0) {
                    timeText = (countdownTime / 60000) + "";
                } else {
                    timeText = "0";
                }

                timeTextView.setText(timeText);
                minuteTextView.setText(context.getResources().getString(R.string.min));
            } else {
                timeText = new SimpleDateFormat("h:mm").format(predictionTime);
                minuteText = new SimpleDateFormat("a").format(predictionTime).toLowerCase();

                timeTextView.setText(timeText);
                minuteTextView.setText(minuteText);
            }

            if (prediction.isLive()) {
                Drawable border = context.getResources().getDrawable(R.drawable.rounded_border);
                DrawableCompat.setTint(border, context.getResources().getColor(R.color.livePrediction));
                statusTextView.setBackground(border);

                String statusText = context.getResources().getString(R.string.live);
                statusTextView.setText(statusText);
                statusTextView.setTextColor(context.getResources().getColor(R.color.livePrediction));

                statusTextView.setVisibility(VISIBLE);
            }
        } else {
            timeTextView.setText("---");
            minuteTextView.setVisibility(GONE);
        }
    }
}
