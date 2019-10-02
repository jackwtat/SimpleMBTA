package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
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

    View rootView;
    View topLineView;
    View bottomLineView;
    ImageView stopIcon;
    ImageView stopIconFill;
    TextView stopName;
    TextView timeTextView;
    TextView minuteTextView;

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

    public void setPrediction(Prediction prediction, int stopSequence, boolean showCountdown) {
        long countdownTime = prediction.getCountdownTime() + 15000;
        String timeText;
        String minuteText;

        if (showCountdown && countdownTime >= 0) {
            timeText = (countdownTime / 60000) + "";
            minuteText = min;

            timeTextView.setText(timeText);
            minuteTextView.setText(minuteText);
            minuteTextView.setVisibility(VISIBLE);

        } else {
            Date predictionTime = prediction.getPredictionTime();
            timeText = new SimpleDateFormat("h:mm").format(predictionTime);
            minuteText = new SimpleDateFormat("a").format(predictionTime).toLowerCase();

            timeTextView.setText(timeText);
            minuteTextView.setText(minuteText);
            minuteTextView.setVisibility(VISIBLE);
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
        DrawableCompat.setTint(fill, getContext().getResources().getColor(R.color.list_view_background));
        stopIconFill.setVisibility(VISIBLE);

        // Set line visibility
        if (stopSequence == FIRST_STOP) {
            topLineView.setVisibility(INVISIBLE);
            bottomLineView.setVisibility(VISIBLE);

        } else if (stopSequence == LAST_STOP) {
            topLineView.setVisibility(VISIBLE);
            bottomLineView.setVisibility(INVISIBLE);

        } else {
            topLineView.setVisibility(VISIBLE);
            bottomLineView.setVisibility(VISIBLE);
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
        timeTextView.setText("");
        minuteTextView.setText("");
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.item_trip_detail_prediction, this);
        topLineView = rootView.findViewById(R.id.top_line_view);
        bottomLineView = rootView.findViewById(R.id.bottom_line_view);
        stopIcon = rootView.findViewById(R.id.stop_icon);
        stopIconFill = rootView.findViewById(R.id.stop_icon_fill);
        stopName = rootView.findViewById(R.id.stop_name_text_view);
        timeTextView = rootView.findViewById(R.id.time_text_view);
        minuteTextView = rootView.findViewById(R.id.minute_text_view);

        min = context.getResources().getString(R.string.min);
    }
}
