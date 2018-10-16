package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Route;

public class RouteNameView extends RelativeLayout {
    final static public int SQUARE_BACKGROUND = 0;
    final static public int ROUNDED_BACKGROUND = 1;

    View rootView;
    View routeNameAccentView;
    TextView routeNameTextView;

    public RouteNameView(Context context) {
        super(context);
        initializeViews(context);
    }

    public RouteNameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public RouteNameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews(context);
    }

    public RouteNameView(Context context, Route route, float textSize, int backgroundShape,
                         boolean abbreviateName, boolean enableColorAccent) {
        super(context);
        initializeViews(context);
        setRouteNameView(route, textSize, backgroundShape, abbreviateName, enableColorAccent);
    }

    public void setRouteNameView(Route route, float textSize, int backgroundShape,
                                 boolean abbreviateName, boolean enableColorAccent) {
        setBackground(route, backgroundShape);
        setColorAccent(route, enableColorAccent);
        setRouteName(route, abbreviateName, textSize);
    }

    private void setBackground(Route route, int backgroundShape) {
        Drawable background;

        // Background shape
        if (backgroundShape == ROUNDED_BACKGROUND) {
            background = getContext().getResources().getDrawable(R.drawable.rounded_background);
        } else {
            background = getContext().getResources().getDrawable(R.drawable.square_background);
        }

        // Background color
        DrawableCompat.setTint(background, Color.parseColor(route.getPrimaryColor()));

        // Set background
        setBackground(background);
    }

    private void setColorAccent(Route route, boolean enableColorAccent) {
        if (enableColorAccent && route.getMode() == Route.BUS &&
                !route.getLongName().contains("Silver Line") && !route.getShortName().contains("SL")) {
            routeNameAccentView.setVisibility(View.VISIBLE);
        } else {
            routeNameAccentView.setVisibility(View.GONE);
        }
    }

    private void setRouteName(Route route, boolean abbreviateName, float textSize) {
        routeNameTextView.setTextColor(Color.parseColor(route.getTextColor()));
        routeNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        if (abbreviateName) {
            routeNameTextView.setText(route.getShortDisplayName(getContext()));
        } else {
            routeNameTextView.setText(route.getLongDisplayName(getContext()));
        }
    }

    private void initializeViews(Context context) {
        rootView = inflate(context, R.layout.route_name_view, this);
        routeNameAccentView = rootView.findViewById(R.id.route_name_accent);
        routeNameTextView = rootView.findViewById(R.id.route_name_text_view);
    }
}
