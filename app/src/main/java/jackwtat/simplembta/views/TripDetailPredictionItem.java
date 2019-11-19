package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Prediction;

public class TripDetailPredictionItem extends LinearLayout {

    public static final int FIRST_STOP = 0;
    public static final int INTERMEDIATE_STOP = 1;
    public static final int LAST_STOP = 2;
    public static final int ONLY_STOP = 3;

    View rootView;
    View topLineView;
    View bottomLineView;
    ImageView stopIcon;
    ImageView stopIconFill;
    ImageView wheelchairAccessibleIcon;
    TextView stopName;
    TextView timeTextView;
    TextView minuteTextView;
    TextView statusTextView;

    String min;

    public TripDetailPredictionItem(Context context) {
        super(context);
        init(context);
    }

    public TripDetailPredictionItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TripDetailPredictionItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setPrediction(
            Prediction prediction, int stopSequenceType, int vehicleStopSequence,
            String selectedTripId, String vehicleTripId) {

        long countdownTime = prediction.getCountdownTime();

        // Vehicle is currently on selected trip
        if (selectedTripId.equals(vehicleTripId)) {

            // Vehicle has already passed selected stop
            if (vehicleStopSequence > prediction.getStopSequence()) {
                String statusText;

                if (stopSequenceType == LAST_STOP || stopSequenceType == ONLY_STOP) {
                    statusText = getContext().getResources()
                            .getString(R.string.trip_already_arrived);
                } else {
                    statusText = getContext().getResources()
                            .getString(R.string.trip_already_departed);
                }

                statusTextView.setText(statusText);

                timeTextView.setVisibility(GONE);
                minuteTextView.setVisibility(GONE);
                statusTextView.setVisibility(VISIBLE);

                // Vehicle is currently at or approaching selected stop
            } else if (vehicleStopSequence == prediction.getStopSequence()) {
                if (countdownTime / 60000 > 0) {
                    String timeText = (countdownTime / 60000) + "";
                    String minuteText = min;

                    timeTextView.setText(timeText);
                    minuteTextView.setText(minuteText);

                    timeTextView.setVisibility(VISIBLE);
                    minuteTextView.setVisibility(VISIBLE);
                    statusTextView.setVisibility(GONE);

                } else if (countdownTime > -30000) {
                    statusTextView.setText(getContext().getResources().getString(R.string.trip_arriving));

                    timeTextView.setVisibility(GONE);
                    minuteTextView.setVisibility(GONE);
                    statusTextView.setVisibility(VISIBLE);

                } else {
                    statusTextView.setText(getContext().getResources().getString(R.string.trip_already_departed));

                    timeTextView.setVisibility(GONE);
                    minuteTextView.setVisibility(GONE);
                    statusTextView.setVisibility(VISIBLE);
                }

                // Vehicle has not yet reached selected stop
            } else {
                String timeText;
                if (countdownTime > 0) {
                    timeText = (countdownTime / 60000) + "";
                } else {
                    timeText = "0";
                }
                String minuteText = min;

                timeTextView.setText(timeText);
                minuteTextView.setText(minuteText);

                timeTextView.setVisibility(VISIBLE);
                minuteTextView.setVisibility(VISIBLE);
                statusTextView.setVisibility(GONE);
            }

            // Vehicle is not on selected trip
        } else {
            if (countdownTime > 0) {
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

                timeTextView.setVisibility(VISIBLE);
                minuteTextView.setVisibility(VISIBLE);
                statusTextView.setVisibility(GONE);

            } else {
                String statusText;

                if (stopSequenceType == LAST_STOP || stopSequenceType == ONLY_STOP) {
                    statusText = getContext().getResources()
                            .getString(R.string.trip_already_arrived);
                } else {
                    statusText = getContext().getResources()
                            .getString(R.string.trip_already_departed);
                }

                statusTextView.setText(statusText);

                timeTextView.setVisibility(GONE);
                minuteTextView.setVisibility(GONE);
                statusTextView.setVisibility(VISIBLE);
            }
        }

        // Show stop name
        stopName.setText(prediction.getStop().getName());

        // Set line colors
        Drawable background = topLineView.getBackground();
        DrawableCompat.setTint(background, Color.parseColor(prediction.getRoute().getPrimaryColor()));
        topLineView.setBackground(background);
        bottomLineView.setBackground(background);

        // Set stop icon color
        Drawable icon = stopIcon.getDrawable();
        DrawableCompat.setTint(icon, Color.parseColor(prediction.getRoute().getPrimaryColor()));
        stopIcon.setBackground(icon);

        Drawable fill = stopIconFill.getDrawable();
        DrawableCompat.setTint(fill, Color.WHITE);
        stopIconFill.setVisibility(VISIBLE);

        // Set line visibility
        if (stopSequenceType == FIRST_STOP) {
            topLineView.setVisibility(INVISIBLE);
            bottomLineView.setVisibility(VISIBLE);

        } else if (stopSequenceType == LAST_STOP) {
            topLineView.setVisibility(VISIBLE);
            bottomLineView.setVisibility(INVISIBLE);

        } else if (stopSequenceType == ONLY_STOP) {
            topLineView.setVisibility(INVISIBLE);
            bottomLineView.setVisibility(INVISIBLE);

        } else {
            topLineView.setVisibility(VISIBLE);
            bottomLineView.setVisibility(VISIBLE);
        }

        // Set wheelchair accessibility icon visibility
        if (prediction.getStop().isWheelchairAccessible()) {
            wheelchairAccessibleIcon.setVisibility(VISIBLE);
        } else {
            wheelchairAccessibleIcon.setVisibility(GONE);
        }
    }

    public void emphasize() {
        stopName.setTypeface(stopName.getTypeface(), Typeface.BOLD_ITALIC);
    }

    public void clear() {
        topLineView.setVisibility(INVISIBLE);
        bottomLineView.setVisibility(INVISIBLE);
        stopName.setText("");
        stopName.setTypeface(Typeface.DEFAULT);
        wheelchairAccessibleIcon.setVisibility(GONE);
        timeTextView.setText("");
        minuteTextView.setText("");
        statusTextView.setText("");
        statusTextView.setVisibility(GONE);
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.item_trip_detail_prediction, this);
        topLineView = rootView.findViewById(R.id.top_line_view);
        bottomLineView = rootView.findViewById(R.id.bottom_line_view);
        stopIcon = rootView.findViewById(R.id.stop_icon);
        stopIconFill = rootView.findViewById(R.id.stop_icon_fill);
        stopName = rootView.findViewById(R.id.stop_name_text_view);
        wheelchairAccessibleIcon = rootView.findViewById(R.id.wheelchair_accessible_icon);
        timeTextView = rootView.findViewById(R.id.time_text_view);
        minuteTextView = rootView.findViewById(R.id.minute_text_view);
        statusTextView = rootView.findViewById(R.id.status_text_view);

        min = context.getResources().getString(R.string.min);
    }
}
