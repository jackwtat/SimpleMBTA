package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jackwtat.simplembta.R;
import jackwtat.simplembta.mbta.structure.Mode;
import jackwtat.simplembta.mbta.structure.Route;

public class RouteNameView extends RelativeLayout {

    final static public int SQUARE_BACKGROUND = 0;
    final static public int ROUNDED_BACKGROUND = 1;
    final static public int SMALL_TEXT_SIZE = 22;
    final static public int LARGE_TEXT_SIZE = 26;

    Route route;
    View rootView;
    View routeNameAccentView;
    TextView routeNameTextView;
    Drawable background;

    public RouteNameView(Context context) {
        super(context);
        initializeViews(context, SQUARE_BACKGROUND);
    }

    public RouteNameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context, SQUARE_BACKGROUND);
    }

    public RouteNameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews(context, SQUARE_BACKGROUND);
    }

    public RouteNameView(Context context, Route route, int textSize, int backgroundShape,
                         boolean abbreviateName, boolean enableColorAccent) {
        super(context);
        initializeViews(context, backgroundShape);

        if (enableColorAccent && route.getMode() == Mode.BUS &&
                !route.getLongName().contains("Silver Line")) {
            enableColorAccent();
        } else {
            disableColorAccent();
        }

        setTextSize(textSize);

        setRoute(route, abbreviateName);
    }

    public void setRoute(Route route, boolean abbreviateName) {
        this.route = route;

        if (abbreviateName) {
            routeNameTextView.setText(route.getShortDisplayName(getContext()));
        } else {
            routeNameTextView.setText(route.getLongDisplayName(getContext()));
        }

        routeNameTextView.setTextColor(Color.parseColor(route.getTextColor()));

        DrawableCompat.setTint(background, Color.parseColor(route.getPrimaryColor()));
    }

    public void setTextSize(int textSize){
        routeNameTextView.setTextSize(textSize);
    }

    public void enableColorAccent() {
        routeNameAccentView.setVisibility(View.VISIBLE);
    }

    public void disableColorAccent() {
        routeNameAccentView.setVisibility(View.GONE);
    }

    public void setBackground(int backgroundShape) {
        if (backgroundShape == ROUNDED_BACKGROUND) {
            background = getContext().getResources().getDrawable(R.drawable.rounded_background);
        } else {
            background = getContext().getResources().getDrawable(R.drawable.square_background);
        }
        if (route != null){
            DrawableCompat.setTint(background, Color.parseColor(route.getPrimaryColor()));
        }

        setBackground(background);
    }

    public Route getRoute() {
        return route;
    }

    private void initializeViews(Context context, int backgroundShape) {
        rootView = inflate(context, R.layout.route_name_view, this);
        routeNameTextView = rootView.findViewById(R.id.route_name_text_view);
        routeNameAccentView = rootView.findViewById(R.id.route_name_accent);

        setBackground(backgroundShape);
    }
}
