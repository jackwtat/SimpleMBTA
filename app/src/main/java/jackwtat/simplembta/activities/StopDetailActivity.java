package jackwtat.simplembta.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import jackwtat.simplembta.R;
import jackwtat.simplembta.adapters.StopRecyclerViewAdapter;
import jackwtat.simplembta.clients.NetworkConnectivityClient;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.utilities.Constants;
import jackwtat.simplembta.utilities.ErrorManager;
import jackwtat.simplembta.views.NoPredictionsView;

class StopDetailActivity extends AppCompatActivity implements
        ErrorManager.OnErrorChangedListener, Constants {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private NoPredictionsView noPredictionsView;
    private TextView errorTextView;

    private String realTimeApiKey;
    private NetworkConnectivityClient networkConnectivityClient;
    private ErrorManager errorManager;
    private StopRecyclerViewAdapter recyclerViewAdapter;
    private Timer timer;

    // TODO: Create async tasks

    private boolean dataRefreshing = false;
    private boolean loaded = false;
    private boolean userIsScrolling = false;
    private long refreshTime = 0;

    private ArrayList<Prediction> predictions = new ArrayList<>();
    private Stop selectedStop;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_detail);

        // Get MBTA realTime API key
        realTimeApiKey = getResources().getString(R.string.v3_mbta_realtime_api_key_general);

        // Get network connectivity client
        networkConnectivityClient = new NetworkConnectivityClient(this);

        // Get data saved from previous session
        if (savedInstanceState != null) {
            selectedStop = (Stop) savedInstanceState.getSerializable("stop");
            refreshTime = savedInstanceState.getLong("refreshTime");

            if (new Date().getTime() - refreshTime > MAXIMUM_PREDICTION_AGE) {
                predictions.clear();
            }

            // Get values passed from calling activity/fragment
        } else {
            Intent intent = getIntent();
            selectedStop = (Stop) intent.getSerializableExtra("stop");
            refreshTime = intent.getLongExtra("refreshTime", MAXIMUM_PREDICTION_AGE + 1);
        }

        // Set action bar title
        String title = selectedStop.getName();
        setTitle(title);

        // Get error text view
        errorTextView = findViewById(R.id.error_message_text_view);

        // Set the no predictions indicator
        noPredictionsView = findViewById(R.id.no_predictions_view);

        // Get and initialize swipe refresh layout
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this,
                R.color.colorAccent));
        swipeRefreshLayout.setEnabled(false);

        // Get and set recycler view
        recyclerView = findViewById(R.id.predictions_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        // Disable scrolling while activity is still initializing
        recyclerView.setNestedScrollingEnabled(false);

        // Add on scroll listener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    userIsScrolling = false;
                    if (!dataRefreshing && !noPredictionsView.isError()) {
                        refreshPredictions();
                    }

                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    userIsScrolling = true;
                }
            }
        });

        // Create and set the recycler view adapter
        recyclerViewAdapter = new StopRecyclerViewAdapter();
        recyclerView.setAdapter(recyclerViewAdapter);

        // TODO: OnClickListeners
    }

    @Override
    protected void onStart() {
        super.onStart();

        errorManager = ErrorManager.getErrorManager();
        errorManager.registerOnErrorChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh the activity to update UI so that the predictions are accurate
        // as of the last update
        refreshPredictions();

        // If too much time has elapsed since last refresh, then clear predictions and force update
        if (new Date().getTime() - refreshTime > MAXIMUM_PREDICTION_AGE) {
            clearPredictions();
            swipeRefreshLayout.setRefreshing(true);
            forceUpdate();

            // If there are no predictions displayed in the recycler view, then force a refresh
        } else if (recyclerViewAdapter.getItemCount() < 1) {
            swipeRefreshLayout.setRefreshing(true);
            forceUpdate();

            // Otherwise, background update
        } else {
            backgroundUpdate();
        }

        // Set timer
        timer = new Timer();
        timer.schedule(new PredictionsUpdateTimerTask(), 0, PREDICTIONS_UPDATE_RATE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        dataRefreshing = false;

        swipeRefreshLayout.setRefreshing(false);

        if (timer != null) {
            timer.cancel();
        }

        cancelUpdate();
    }

    @Override
    public void onErrorChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                errorTextView.setOnClickListener(null);

                if (errorManager.hasNetworkError()) {
                    errorTextView.setText(R.string.network_error_text);
                    errorTextView.setVisibility(View.VISIBLE);

                    clearPredictions();
                    enableOnErrorView(getResources().getString(R.string.network_error_text));

                } else if (errorManager.hasTimeZoneMismatch()) {
                    errorTextView.setText(R.string.time_zone_warning);
                    errorTextView.setVisibility(View.VISIBLE);

                } else if (!errorManager.hasNetworkError()) {
                    errorTextView.setVisibility(View.GONE);
                    clearOnErrorView();
                    swipeRefreshLayout.setRefreshing(true);
                    forceUpdate();
                }
            }
        });
    }

    private void enableOnErrorView(final String message){

    }

    private void clearOnErrorView(){

    }

    private void clearPredictions(){

    }

    private void refreshPredictions(){

    }

    private void backgroundUpdate(){

    }

    private void forceUpdate(){

    }

    private void cancelUpdate(){

    }


    private class PredictionsUpdateTimerTask extends TimerTask {
        @Override
        public void run() {
            backgroundUpdate();
        }
    }
}
