package jackwtat.simplembta.adapters.spinners;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.views.RouteNameView;

public class RoutesSpinnerAdapter extends ArrayAdapter<Route> {
    Context context;
    Route[] routes;
    Route selectedRoute;

    public RoutesSpinnerAdapter(Context context, Route[] routes) {
        super(context, 0, routes);
        this.context = context;
        this.routes = routes;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(routes[position], parent, R.layout.spinner_item_route_selected);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem;
        Route route = routes[position];

        if (route.equals(selectedRoute)) {
            listItem = createItemView(route, parent, R.layout.spinner_item_route_dropdown_selected);
        } else {
            listItem = createItemView(route, parent, R.layout.spinner_item_route_dropdown);
        }

        return listItem;
    }

    private View createItemView(Route route, @NonNull ViewGroup parent, @NonNull int layout) {
        View listItem = LayoutInflater.from(context).inflate(layout, parent, false);

        RouteNameView nameTextView = listItem.findViewById(R.id.route_name_view);

        TextView routeDescriptionTextView = listItem.findViewById(R.id.route_description_text_view);

        if (route != null) {
            nameTextView.setRouteNameView(route);

            if (route.getMode() == Route.BUS) {
                routeDescriptionTextView.setText(route.getLongName());
                routeDescriptionTextView.setVisibility(View.VISIBLE);
            } else {
                routeDescriptionTextView.setVisibility(View.GONE);
            }
        } else {
            nameTextView.setRouteNameView(new Route("null"));
        }

        return listItem;
    }

    @Nullable
    @Override
    public Route getItem(int position) {
        return routes[position];
    }

    public void setSelectedRoute(Route route) {
        selectedRoute = route;
    }
}
