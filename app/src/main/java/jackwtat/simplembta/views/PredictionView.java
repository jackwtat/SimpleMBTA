package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import jackwtat.simplembta.R;
import jackwtat.simplembta.mbta.structure.Mode;
import jackwtat.simplembta.mbta.structure.Prediction;

public class PredictionView extends LinearLayout {
    View rootView;

    TextView destinationView;
    TextView firstTimeView;
    TextView secondTimeView;

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
        init(context, prediction, null);
    }

    public PredictionView(Context context, Prediction prediction1, Prediction prediction2) {
        super(context);
        init(context, prediction1, prediction2);
    }

    private void init(Context context, Prediction p1, @Nullable Prediction p2) {
        rootView = inflate(context, R.layout.prediction_view, this);

        destinationView = rootView.findViewById(R.id.destination_text_view);
        firstTimeView = rootView.findViewById(R.id.first_time_text_view);
        secondTimeView = rootView.findViewById(R.id.second_time_text_view);

        trackNumberView = rootView.findViewById(R.id.track_number_text_view);

        destinationView.setText(p1.getTrip().getDestination());

        // Set track number
        if (p1.getRoute().getMode() == Mode.COMMUTER_RAIL &&
                !p1.getTrackNumber().equals("null")) {
            String trackNumber = context.getResources().getString(R.string.track) + " " +
                    p1.getTrackNumber();
            trackNumberView.setText(trackNumber);

            Drawable background = context.getResources().getDrawable(R.drawable.rounded_background);
            DrawableCompat.setTint(background, context.getResources().getColor(R.color.ApproachingAlert));
            trackNumberView.setBackground(background);
        } else {
            trackNumberView.setVisibility(GONE);
        }

        // Set departure times
        if (p1.getDepartureTime() != null)

        {
            String dept_1;
            String dept_2;

            if (p1.getTimeUntilDeparture() > 0) {
                dept_1 = (p1.getTimeUntilDeparture() / 60000) + "";
            } else {
                dept_1 = "0";
            }

            if (p2 != null && p2.getDepartureTime() != null) {
                dept_1 = dept_1 + ",";
                if (p2.getTimeUntilDeparture() > 0) {
                    dept_2 = (p2.getTimeUntilDeparture() / 60000) + " min";
                } else {
                    dept_2 = "0 min";
                }
            } else {
                dept_2 = "min";
            }

            firstTimeView.setText(dept_1);
            secondTimeView.setText(dept_2);
        } else

        {
            firstTimeView.setText("---");
            secondTimeView.setVisibility(GONE);
        }
    }
}
