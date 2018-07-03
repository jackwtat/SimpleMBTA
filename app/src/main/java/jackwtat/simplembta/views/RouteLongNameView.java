package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jackwtat.simplembta.R;
import jackwtat.simplembta.mbta.structure.Mode;
import jackwtat.simplembta.mbta.structure.Route;

public class RouteLongNameView extends RelativeLayout {
    View rootView;
    TextView routeName;
    View routeNameAccent;

    public RouteLongNameView(Context context) {
        super(context);
    }

    public RouteLongNameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RouteLongNameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RouteLongNameView(Context context, Route route) {
        super(context);

        rootView = inflate(context, R.layout.route_long_name_view, this);
        routeName = rootView.findViewById(R.id.alert_route_name);
        routeNameAccent = rootView.findViewById(R.id.alert_route_name_accent);

        routeName.setBackgroundColor(Color.parseColor(route.getPrimaryColor()));
        routeName.setTextColor(Color.parseColor(route.getTextColor()));

        routeName.setText(route.getDisplayName(context));

        if (route.getMode() == Mode.BUS) {
            routeNameAccent.setVisibility(View.VISIBLE);
        } else {
            routeNameAccent.setVisibility(View.GONE);
        }
    }
}
