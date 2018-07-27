package jackwtat.simplembta.fragments;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jackwtat.simplembta.R;
import jackwtat.simplembta.controllers.NearbyPredictionsController;
import jackwtat.simplembta.controllers.listeners.OnLocationErrorListener;
import jackwtat.simplembta.controllers.listeners.OnNetworkErrorListener;
import jackwtat.simplembta.controllers.listeners.OnPostExecuteListener;
import jackwtat.simplembta.controllers.listeners.OnProgressUpdateListener;
import jackwtat.simplembta.mbta.structure.Stop;
import jackwtat.simplembta.views.PredictionsListView;


/**
 * Created by jackw on 8/21/2017.
 */

public class NearbyPredictionsFragment extends RefreshableFragment {
    private final static String LOG_TAG = "NearbyPredsFragment";

    private final long AUTO_REFRESH_RATE = 60000;

    private View rootView;
    private PredictionsListView predictionsListView;

    private NearbyPredictionsController controller;
    private Timer autoRefreshTimer;

    private boolean resetUI = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        controller = new NearbyPredictionsController(getContext(),
                new OnPostExecuteListener() {
                    public void onPostExecute(List<Stop> stops) {
                        try {
                            predictionsListView.publishPredictions(getContext(), stops, resetUI);
                        } catch (IllegalStateException e) {
                            Log.e(LOG_TAG, "Pushing get results to nonexistent view");
                        }
                    }
                },
                new OnProgressUpdateListener() {
                    public void onProgressUpdate(int progress) {
                        try {
                            predictionsListView.setRefreshProgress();
                        } catch (IllegalStateException e) {
                            Log.e(LOG_TAG, "Pushing progress update to nonexistent view");
                        }
                    }
                },
                new OnNetworkErrorListener() {
                    public void onNetworkError() {
                        predictionsListView.onRefreshError(getResources().getString(R.string.no_network_connectivity));
                    }
                },
                new OnLocationErrorListener() {
                    public void onLocationError() {
                        predictionsListView.onRefreshError(getResources().getString(R.string.no_location));
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_nearby_predictions, container, false);

        predictionsListView = rootView.findViewById(R.id.predictions_list_view);
        predictionsListView.setOnSwipeRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                forceRefresh();
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        autoRefreshTimer = new Timer();
        autoRefreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        autoRefresh();
                    }
                });
            }
        }, AUTO_REFRESH_RATE, AUTO_REFRESH_RATE);
    }

    @Override
    public void onResume() {
        super.onResume();

        controller.connect();
        refresh();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Hide alert dialog
        predictionsListView.hideAlertsDialog();

    }

    @Override
    public void onStop() {
        if (controller.isRunning()) {
            predictionsListView.onRefreshCanceled();
        }

        autoRefreshTimer.cancel();
        controller.cancel();

        super.onStop();
    }

    @Override
    public void refresh() {
        resetUI = true;
        controller.update();
    }

    @Override
    public void autoRefresh() {
        resetUI = false;
        controller.update();
    }

    @Override
    public void forceRefresh() {
        resetUI = true;
        controller.forceUpdate();
    }
}
