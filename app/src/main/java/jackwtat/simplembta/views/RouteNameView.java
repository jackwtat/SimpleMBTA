package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import jackwtat.simplembta.R;
import jackwtat.simplembta.mbta.structure.Mode;
import jackwtat.simplembta.mbta.structure.Route;


public class RouteNameView extends AppCompatTextView {
    public RouteNameView(Context context) {
        super(context);
    }

    public RouteNameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RouteNameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setRouteName(Context context, Route route) {
        Drawable background = context.getResources().getDrawable(R.drawable.route_background);
        DrawableCompat.setTint(background, Color.parseColor(route.getColor()));

        setBackground(background);
        setTextColor(Color.parseColor(route.getTextColor()));

        String routeId = route.getId();
        Mode mode = route.getMode();

        if (mode == Mode.HEAVY_RAIL) {
            if (routeId.equals("Red"))
                setText(context.getResources().getString(R.string.red_line_short_name));
            else if (routeId.equals("Orange"))
                setText(context.getResources().getString(R.string.orange_line_short_name));
            else if (routeId.equals("Blue"))
                setText(context.getResources().getString(R.string.blue_line_short_name));
            else
                setText(routeId);

        } else if (mode == Mode.LIGHT_RAIL) {
            if (routeId.equals("Green-B"))
                setText(context.getResources().getString(R.string.green_line_b_short_name));
            else if (routeId.equals("Green-C"))
                setText(context.getResources().getString(R.string.green_line_c_short_name));
            else if (routeId.equals("Green-D"))
                setText(context.getResources().getString(R.string.green_line_d_short_name));
            else if (routeId.equals("Green-E"))
                setText(context.getResources().getString(R.string.green_line_e_short_name));
            else if (routeId.equals("Mattapan"))
                setText(context.getResources().getString(R.string.red_line_mattapan_short_name));
            else
                setText(routeId);

        } else if (mode == Mode.BUS) {
            if (routeId.equals("746"))
                setText(context.getResources().getString(R.string.silver_line_waterfront_short_name));
            else if (!route.getShortName().equals("") && !route.getShortName().equals("null"))
                setText(route.getShortName());
            else
                setText(routeId);

        } else if (mode == Mode.COMMUTER_RAIL) {
            setText(context.getResources().getString(R.string.commuter_rail_short_name));

        } else if (mode == Mode.FERRY) {
            setText(context.getResources().getString(R.string.ferry_short_name));

        } else {
            setText(routeId);
        }
    }
}
