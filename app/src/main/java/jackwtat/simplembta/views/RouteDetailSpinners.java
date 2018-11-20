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
import jackwtat.simplembta.adapters.spinners.StopsSpinnerAdapter;
import jackwtat.simplembta.model.Direction;
import jackwtat.simplembta.model.Stop;

public class RouteDetailSpinners extends LinearLayout implements AdapterView.OnItemSelectedListener {
    private View rootView;
    private Spinner directionSpinner;
    private Spinner stopSpinner;
    private DirectionsSpinnerAdapter directionsAdapter;
    private StopsSpinnerAdapter stopsAdapter;

    private Direction[] directions = {};
    private Stop[] stops = {};

    private OnDirectionSelectedListener onDirectionSelectedListener;
    private OnStopSelectedListener onStopSelectedListener;

    public RouteDetailSpinners(Context context) {
        super(context);
        init(context);
    }

    public RouteDetailSpinners(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RouteDetailSpinners(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.spinners_route_detail, this);

        directionSpinner = rootView.findViewById(R.id.direction_spinner);
        directionSpinner.setOnItemSelectedListener(this);

        stopSpinner = rootView.findViewById(R.id.stop_spinner);
        stopSpinner.setOnItemSelectedListener(this);
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

    public void setOnDirectionSelectedListener(OnDirectionSelectedListener onDirectionSelectedListener) {
        this.onDirectionSelectedListener = onDirectionSelectedListener;
    }

    public void setOnStopSelectedListener(OnStopSelectedListener onStopSelectedListener) {
        this.onStopSelectedListener = onStopSelectedListener;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
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

    public interface OnDirectionSelectedListener {
        void onDirectionSelected(Direction direction);
    }

    public interface OnStopSelectedListener {
        void onStopSelected(Stop stop);
    }
}
