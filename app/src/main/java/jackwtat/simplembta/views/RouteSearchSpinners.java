package jackwtat.simplembta.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import jackwtat.simplembta.R;
import jackwtat.simplembta.adapters.spinners.DirectionsSpinnerAdapter;
import jackwtat.simplembta.adapters.spinners.RoutesSpinnerAdapter;
import jackwtat.simplembta.adapters.spinners.StopsSpinnerAdapter;
import jackwtat.simplembta.model.Direction;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.model.Route;

public class RouteSearchSpinners extends LinearLayout implements AdapterView.OnItemSelectedListener {
    private View rootView;
    private LinearLayout routeLayout;
    private LinearLayout directionLayout;
    private LinearLayout stopLayout;
    private Spinner routeSpinner;
    private Spinner directionSpinner;
    private Spinner stopSpinner;

    private RoutesSpinnerAdapter routesAdapter;
    private DirectionsSpinnerAdapter directionsAdapter;
    private StopsSpinnerAdapter stopsAdapter;

    private Route[] routes = {};
    private Direction[] directions = {};
    private Stop[] stops = {};

    private OnRouteSelectedListener onRouteSelectedListener;
    private OnDirectionSelectedListener onDirectionSelectedListener;
    private OnStopSelectedListener onStopSelectedListener;

    public RouteSearchSpinners(Context context) {
        super(context);
        init(context);
    }

    public RouteSearchSpinners(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RouteSearchSpinners(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.spinners_route_search, this);

        routeSpinner = rootView.findViewById(R.id.route_spinner);
        routeSpinner.setOnItemSelectedListener(this);

        directionSpinner = rootView.findViewById(R.id.direction_spinner);
        directionSpinner.setOnItemSelectedListener(this);

        stopSpinner = rootView.findViewById(R.id.stop_spinner);
        stopSpinner.setOnItemSelectedListener(this);

        routeLayout = rootView.findViewById(R.id.route_spinner_layout);
        routeLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                routeSpinner.performClick();
            }
        });

        directionLayout = rootView.findViewById(R.id.direction_spinner_layout);
        directionLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                directionSpinner.performClick();
            }
        });

        stopLayout = rootView.findViewById(R.id.stop_spinner_layout);
        stopLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSpinner.performClick();
            }
        });
    }

    public void populateRouteSpinner(Route[] routes) {
        this.routes = routes;
        routesAdapter = new RoutesSpinnerAdapter(getContext(), routes);
        routeSpinner.setAdapter(routesAdapter);
    }

    public void populateDirectionSpinner(Direction[] directions) {
        this.directions = directions;
        directionsAdapter = new DirectionsSpinnerAdapter(getContext(), directions);
        directionSpinner.setAdapter(directionsAdapter);
    }

    public void populateStopSpinner(Stop[] stops) {
        this.stops = stops;
        stopsAdapter = new StopsSpinnerAdapter(getContext(), stops);
        stopSpinner.setAdapter(stopsAdapter);
    }

    public void clearRoutes() {
        populateRouteSpinner(new Route[0]);
    }

    public void clearDirections() {
        populateDirectionSpinner(new Direction[0]);
    }

    public void clearStops() {
        populateStopSpinner(new Stop[0]);
    }

    public void selectRoute(String routeId) {
        for (int i = 0; i < routes.length; i++) {
            if (routes[i].getId().equals(routeId)) {
                routeSpinner.setSelection(i);
            }
        }
    }

    public void selectDirection(int directionId) {
        for (int i = 0; i < directions.length; i++) {
            if (directions[i].getId() == directionId) {
                directionSpinner.setSelection(i);
            }
        }
    }

    public void selectStop(String stopId) {
        for (int i = 0; i < stops.length; i++) {
            if (stops[i].getId().equals(stopId)) {
                stopSpinner.setSelection(i);
                break;
            }
        }
    }

    public void setOnRouteSelectedListener(OnRouteSelectedListener onRouteSelectedListener) {
        this.onRouteSelectedListener = onRouteSelectedListener;
    }

    public void setOnDirectionSelectedListener(OnDirectionSelectedListener onDirectionSelectedListener) {
        this.onDirectionSelectedListener = onDirectionSelectedListener;
    }

    public void setOnStopSelectedListener(OnStopSelectedListener onStopSelectedListener) {
        this.onStopSelectedListener = onStopSelectedListener;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.route_spinner:
                Route selectedRoute = (Route) parent.getItemAtPosition(position);
                routesAdapter.setSelectedRoute(selectedRoute);
                onRouteSelectedListener.onRouteSelected(selectedRoute);
                break;
            case R.id.direction_spinner:
                Direction selectedDirection = (Direction) parent.getItemAtPosition(position);
                directionsAdapter.setSelectedDirection(selectedDirection);
                onDirectionSelectedListener.onDirectionSelected(selectedDirection);
                break;
            case R.id.stop_spinner:
                Stop selectedStop = (Stop) parent.getItemAtPosition(position);
                stopsAdapter.setSelectedStop(selectedStop);
                onStopSelectedListener.onStopSelected(selectedStop);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public interface OnRouteSelectedListener {
        void onRouteSelected(Route route);
    }

    public interface OnDirectionSelectedListener {
        void onDirectionSelected(Direction direction);
    }

    public interface OnStopSelectedListener {
        void onStopSelected(Stop stop);
    }
}
