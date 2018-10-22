package jackwtat.simplembta.views;

import android.content.Context;
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

public class RouteDetailPredictionItem extends LinearLayout {
    View rootView;
    TextView timeTextView;
    TextView minuteTextView;
    TextView liveIndicator;
    TextView dropOffIndicator;
    TextView destinationTextView;

    String min;

    public RouteDetailPredictionItem(Context context) {
        super(context);
        init(context);
    }

    public RouteDetailPredictionItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RouteDetailPredictionItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setPrediction(Prediction prediction) {
        String timeText;
        String minuteText;

        long countdownTime = prediction.getCountdownTime();

        if (countdownTime < 90 * 60000) {
            timeText = (countdownTime / 60000) + "";
            minuteText = min;
        } else {
            Date predictionTime = prediction.getPredictionTime();
            timeText = new SimpleDateFormat("h:mm").format(predictionTime);
            minuteText = new SimpleDateFormat("a").format(predictionTime).toLowerCase();
        }

        timeTextView.setText(timeText);
        minuteTextView.setText(minuteText);

        // Show the appropriate status indicators
        if (!prediction.willPickUpPassengers()) {
            dropOffIndicator.setVisibility(VISIBLE);
        }
        if (prediction.isLive()) {
            liveIndicator.setVisibility(VISIBLE);
        }

        destinationTextView.setText(prediction.getDestination());
    }

    public void clear() {
        timeTextView.setText("");
        minuteTextView.setText("");
        liveIndicator.setVisibility(INVISIBLE);
        dropOffIndicator.setVisibility(GONE);
        destinationTextView.setText("");
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.item_route_detail_prediction, this);
        timeTextView = rootView.findViewById(R.id.time_text_view);
        minuteTextView = rootView.findViewById(R.id.minute_text_view);
        liveIndicator = rootView.findViewById(R.id.live_text_view);
        dropOffIndicator = rootView.findViewById(R.id.drop_off_text_view);
        destinationTextView = rootView.findViewById(R.id.destination_text_view);

        int liveColor = context.getResources().getColor(R.color.livePrediction);
        int dropOffColor = context.getResources().getColor(R.color.dropOffPrediction);

        liveIndicator.setTextColor(liveColor);
        dropOffIndicator.setTextColor(context.getResources().getColor(R.color.HighlightedText));
        DrawableCompat.setTint(liveIndicator.getBackground(), liveColor);
        DrawableCompat.setTint(dropOffIndicator.getBackground(), dropOffColor);

        min = context.getResources().getString(R.string.min);
    }
}
