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

public class IndividualPredictionItem extends LinearLayout {
    View rootView;
    TextView destinationTextView;
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
        if (prediction.willPickUpPassengers()) {
            String timeText;
            String minuteText;

            Date predictionTime;
            long countdownTime;

            predictionTime = prediction.getPredictionTime();
            countdownTime = prediction.getCountdownTime();

            if (countdownTime < 90 * 60000) {
                if (countdownTime > 0) {
                    timeText = (countdownTime / 60000) + "";
                } else {
                    timeText = "0";
                }

                minuteText = min;
            } else {
                timeText = new SimpleDateFormat("h:mm").format(predictionTime);
                minuteText = new SimpleDateFormat("a").format(predictionTime).toLowerCase();
            }

            if (!prediction.isLive()) {
                minuteText += "*";
            }

            timeTextView.setText(timeText);
            minuteTextView.setText(minuteText);
        } else {
            timeTextView.setText("---");
            minuteTextView.setVisibility(GONE);
        }
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.item_individual_prediction, this);
        destinationTextView = rootView.findViewById(R.id.destination_text_view);
        timeTextView = rootView.findViewById(R.id.time_text_view);
        minuteTextView = rootView.findViewById(R.id.minute_text_view);

        min = context.getResources().getString(R.string.min);
    }
}
