package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import jackwtat.simplembta.R;

public class StationNameView extends LinearLayout {
    View rootView;
    View[] secondaryColors;
    TextView stationNameTextView;

    int defaultPrimaryColor;

    public StationNameView(Context context) {
        super(context);
        initializeViews(context);
    }

    public StationNameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public StationNameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews(context);
    }

    public void setText(String name) {
        stationNameTextView.setText(name);
    }

    public void setSecondaryColors(int[] colors) {
        for (int i = 0; i < secondaryColors.length; i++) {
            if (i < colors.length) {
                Drawable background = secondaryColors[i].getBackground();

                DrawableCompat.setTint(background, colors[i]);

                secondaryColors[i].setVisibility(VISIBLE);
            } else {
                secondaryColors[i].setVisibility(GONE);
            }
        }
    }

    public void reset() {
        setText("");
        for (View v : secondaryColors) {
            v.setVisibility(GONE);
        }
    }

    private void initializeViews(Context context) {
        rootView = inflate(context, R.layout.station_name_view, this);
        secondaryColors = new View[3];
        secondaryColors[0] = rootView.findViewById((R.id.secondary_color_0));
        secondaryColors[1] = rootView.findViewById((R.id.secondary_color_1));
        secondaryColors[2] = rootView.findViewById((R.id.secondary_color_2));
        stationNameTextView = rootView.findViewById(R.id.station_name_text_view);
        defaultPrimaryColor = ContextCompat.getColor(context, R.color.error_message_background);
    }
}
