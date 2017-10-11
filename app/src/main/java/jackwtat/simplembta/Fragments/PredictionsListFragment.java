package jackwtat.simplembta.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jackwtat.simplembta.Adapters.IndividualPredictionsListAdapter;
import jackwtat.simplembta.MbtaData.Trip;
import jackwtat.simplembta.R;
import jackwtat.simplembta.MbtaData.Stop;

/**
 * Created by jackw on 8/21/2017.
 */

public abstract class PredictionsListFragment extends UpdatableFragment {
    private final static String LOG_TAG = "PredListsFragment";

    private View rootView;
    private PredictionsListView predictionsListView;
    private TextView updateTimeTextView;
    private TextView statusTextView;
    private TextView debugTextView;
    private ProgressBar progressBar;

    private ArrayAdapter<Trip[]> predictionsListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        predictionsListAdapter = new IndividualPredictionsListAdapter(getActivity(), new ArrayList<Trip[]>());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_predictions_list, container, false);


        predictionsListView = (PredictionsListView) rootView.findViewById(R.id.predictions_list_view);
        updateTimeTextView = (TextView) rootView.findViewById(R.id.updated_time_text_view);
        statusTextView = (TextView) rootView.findViewById(R.id.status_text_view);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        debugTextView = (TextView) rootView.findViewById(R.id.debug_text_view);

        predictionsListView.setAdapter(predictionsListAdapter);

        return rootView;
    }

    protected void updateTime(Date updatedTime){
        TextView updatedTextView = (TextView) rootView.findViewById(R.id.updated_time_text_view);
        SimpleDateFormat ft = new SimpleDateFormat("h:mm a");
        String text = "Updated " + ft.format(updatedTime);
        updatedTextView.setText(text);
    }

    protected  void setDebugTextView(String message){
        debugTextView.setText(message);
    }

    protected String getStatusMessage() {
        return statusTextView.getText().toString();
    }

    protected void setStatusMessage(String status) {
        statusTextView.setText(status);
    }

    protected void showStatusMessage(boolean show) {
        if (show) {
            statusTextView.setVisibility(View.VISIBLE);
        } else {
            statusTextView.setVisibility(View.INVISIBLE);
        }
    }

    protected void showProgressBar(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    protected void populateList(List<Stop> stops) {
        predictionsListAdapter.clear();

        ArrayList<String> trips = new ArrayList<>();

        for (int i = 0; i < stops.size(); i++) {
            Trip[][][] predArray = stops.get(i).getSortedTrips(2);

            for (int j = 0; j < predArray.length; j++) {
                for (int k = 0; k < predArray[j].length; k++) {
                    if (predArray[j][k][0] != null) {
                        if (!trips.contains(predArray[j][k][0].getId())) {
                            predictionsListAdapter.add(predArray[j][k]);
                            trips.add(predArray[j][k][0].getId());
                            if (predArray[j][k][1] != null) {
                                trips.add(predArray[j][k][1].getId());
                            }
                        } else if (predArray[j][k][1] != null) {
                            if (!trips.contains(predArray[j][k][1].getId())) {
                                predictionsListAdapter.add(predArray[j][k]);
                                trips.add(predArray[j][k][0].getId());
                            }
                        }
                    }
                }
            }
        }
    }

    protected void clearList() {
        predictionsListAdapter.clear();
    }

    public class PredictionsListView extends ListView {
        public PredictionsListView(Context context) {
            super(context);
        }

        @Override
        protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
            super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
            update();
        }
    }
}
