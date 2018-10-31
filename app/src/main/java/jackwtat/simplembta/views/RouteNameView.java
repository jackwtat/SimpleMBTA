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
import jackwtat.simplembta.model.Route;

public class RouteNameView extends RelativeLayout {
    View rootView;
    TextView textView;

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

    public RouteNameView(Context context, Route route) {
        super(context);
        initializeViews(context);
        setRouteNameView(context, route);
    }

    public void setRouteNameView(Context context, Route route) {
        // Set text
        if (route.getMode() == Route.BUS) {
            textView.setText(route.getShortDisplayName(context));
        } else {
            textView.setText(route.getLongDisplayName(context));
        }

        // Set text color
        textView.setTextColor(Color.parseColor(route.getTextColor()));

        // Set background color
        Drawable background = textView.getBackground();
        DrawableCompat.setTint(background, Color.parseColor(route.getPrimaryColor()));
        textView.setBackground(background);
    }

    private void initializeViews(Context context) {
        rootView = inflate(context, R.layout.route_name_view, this);
        textView = rootView.findViewById(R.id.route_name_text_view);
    }
}
