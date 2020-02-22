package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import jackwtat.simplembta.R;

public class PredictionHeaderView extends LinearLayout {
    View rootView;
    TextView headerTextView;
    View[] secondaryColors;
    ImageView wheelchairAccessibleIcon;
    ImageView stopAlertIcon;
    ImageView stopAdvisoryIcon;

    int defaultPrimaryColor;

    public PredictionHeaderView(Context context) {
        super(context);
        initializeViews(context);
    }

    public PredictionHeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public PredictionHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews(context);
    }

    public void setText(String name) {
        headerTextView.setText(name);
    }

    public void addSecondaryColor(int color) {
        int i = 0;

        while (i < secondaryColors.length && secondaryColors[i].getVisibility() == VISIBLE) {
            i++;
        }

        if (i < secondaryColors.length && secondaryColors[i].getVisibility() != VISIBLE) {
            Drawable background = secondaryColors[i].getBackground();

            DrawableCompat.setTint(background, color);

            secondaryColors[i].setVisibility(VISIBLE);
        }
    }

    public void setWheelchairAccessible(boolean visible) {
        if (visible) {
            wheelchairAccessibleIcon.setVisibility(VISIBLE);
        } else {
            wheelchairAccessibleIcon.setVisibility(GONE);
        }
    }

    public void enableStopAlertIcon(boolean urgent) {
        if (urgent) {
            stopAlertIcon.setVisibility(VISIBLE);
        } else {
            stopAdvisoryIcon.setVisibility(VISIBLE);
        }
    }

    public void reset() {
        setText("");
        for (View v : secondaryColors) {
            v.setVisibility(GONE);
        }
        wheelchairAccessibleIcon.setVisibility(GONE);
        stopAlertIcon.setVisibility(GONE);
        stopAdvisoryIcon.setVisibility(GONE);
    }

    private void initializeViews(Context context) {
        rootView = inflate(context, R.layout.prediction_header_view, this);
        secondaryColors = new View[3];
        secondaryColors[0] = rootView.findViewById(R.id.secondary_color_0);
        secondaryColors[1] = rootView.findViewById(R.id.secondary_color_1);
        secondaryColors[2] = rootView.findViewById(R.id.secondary_color_2);
        headerTextView = rootView.findViewById(R.id.header_text_view);
        defaultPrimaryColor = ContextCompat.getColor(context, R.color.header_background);
        wheelchairAccessibleIcon = rootView.findViewById(R.id.wheelchair_accessible_icon);
        stopAlertIcon = rootView.findViewById(R.id.stop_alert_icon);
        stopAdvisoryIcon = rootView.findViewById(R.id.stop_advisory_icon);
    }
}
