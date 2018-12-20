package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jackwtat.simplembta.R;

public class StationNameView extends LinearLayout {
    View rootView;
    LinearLayout secondaryColorsLayout;
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

    public void addSecondaryColor(int color) {
        secondaryColorsLayout.addView(new SecondaryColorView(getContext(), color));
    }

    public void reset() {
        setText("");
        secondaryColorsLayout.removeAllViews();
    }

    private void initializeViews(Context context) {
        rootView = inflate(context, R.layout.station_name_view, this);
        secondaryColorsLayout = rootView.findViewById(R.id.secondary_colors_layout);
        stationNameTextView = rootView.findViewById(R.id.station_name_text_view);
        defaultPrimaryColor = ContextCompat.getColor(context, R.color.error_message_background);
    }

    private class SecondaryColorView extends RelativeLayout {
        RelativeLayout rootView;
        TextView someView;

        public SecondaryColorView(Context context) {
            super(context);
            initializeView(context);
        }

        public SecondaryColorView(Context context, AttributeSet attrs) {
            super(context, attrs);
            initializeView(context);
        }

        public SecondaryColorView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            initializeView(context);
        }

        public SecondaryColorView(Context context, int color) {
            super(context);
            initializeView(context);
            setColor(color);
        }

        public void setColor(int color) {
            Drawable background = someView.getBackground().mutate();
            DrawableCompat.setTint(background, color);
            someView.setBackground(background);
        }

        private void initializeView(Context context) {
            rootView = (RelativeLayout) inflate(context, R.layout.station_name_view_secondary_color, this);
            someView = rootView.findViewById(R.id.some_view);
        }
    }
}
