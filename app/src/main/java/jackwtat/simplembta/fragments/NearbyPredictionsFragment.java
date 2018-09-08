package jackwtat.simplembta.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jackwtat.simplembta.adapters.PredictionsAdapter;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.utilities.ErrorManager;
import jackwtat.simplembta.R;
import jackwtat.simplembta.controllers.NearbyPredictionsController;
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.views.AlertsListView;
import jackwtat.simplembta.views.RouteNameView;

public class NearbyPredictionsFragment extends Fragment implements Refreshable {
    private final static String LOG_TAG = "NearbyPredsFragment";

    private final long AUTO_REFRESH_RATE = 45000;

    private View rootView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private AlertDialog alertDialog;
    private TextView noPredictionsTextView;

    private NearbyPredictionsController controller;
    private PredictionsAdapter predictionsAdapter;
    private ErrorManager errorManager;
    private Timer autoRefreshTimer;

    private boolean autoScrollToTop = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        errorManager = ErrorManager.getErrorManager();

        controller = new NearbyPredictionsController(getContext(),
                new NearbyPredictionsController.Callbacks() {
                    public void onPostExecute(List<Route> routes) {
                        predictionsAdapter.setRoutes(routes);

                        if (predictionsAdapter.getItemCount() == 0) {
                            noPredictionsTextView.setVisibility(View.VISIBLE);
                        } else {
                            noPredictionsTextView.setVisibility(View.GONE);
                        }

                        swipeRefreshLayout.setRefreshing(false);

                        if (autoScrollToTop) {
                            recyclerView.scrollToPosition(0);
                        }

                        errorManager.setNetworkError(false);
                        errorManager.setLocationError(false);
                        errorManager.setLocationPermissionDenied(false);
                    }

                    public void onProgressUpdate() {
                        swipeRefreshLayout.setRefreshing(true);
                    }

                    public void onNetworkError() {
                        swipeRefreshLayout.setRefreshing(false);
                        predictionsAdapter.clear();
                        errorManager.setNetworkError(true);
                    }

                    public void onLocationError() {
                        swipeRefreshLayout.setRefreshing(false);
                        predictionsAdapter.clear();
                        errorManager.setLocationError(true);
                    }

                    public void OnLocationPermissionDenied() {
                        swipeRefreshLayout.setRefreshing(false);
                        predictionsAdapter.clear();
                        errorManager.setLocationPermissionDenied(true);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_nearby_predictions, container, false);

        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(),
                R.color.colorAccent));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                forceRefresh();
            }
        });

        noPredictionsTextView = rootView.findViewById(R.id.no_predictions_text_view);

        recyclerView = rootView.findViewById(R.id.predictions_recycler_view);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

        predictionsAdapter = new PredictionsAdapter();
        predictionsAdapter.setOnItemClickListener(new PredictionsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int i) {
                Route route = predictionsAdapter.getRoute(i);

                ArrayList<ServiceAlert> serviceAlerts = route.getServiceAlerts();

                if (serviceAlerts.size() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    Collections.sort(serviceAlerts);

                    RouteNameView routeNameView = new RouteNameView(getContext(), route,
                            getContext().getResources().getDimension(R.dimen.large_route_name_text_size), RouteNameView.SQUARE_BACKGROUND,
                            false, true);
                    routeNameView.setGravity(Gravity.CENTER);

                    builder.setCustomTitle(routeNameView);

                    builder.setView(new AlertsListView(getContext(), serviceAlerts));

                    builder.setPositiveButton(getResources().getString(R.string.dialog_close_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });

        recyclerView.setAdapter(predictionsAdapter);

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

        if (new Date().getTime() - controller.getTimeOfLastRefresh() >
                controller.MAXIMUM_PREDICTION_AGE) {
            predictionsAdapter.clear();
        }

        refresh();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (alertDialog != null) {
            alertDialog.hide();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        controller.cancel();

        autoRefreshTimer.cancel();

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void refresh() {
        autoScrollToTop = true;
        controller.update();
    }

    @Override
    public void autoRefresh() {
        autoScrollToTop = false;
        controller.update();
    }

    @Override
    public void forceRefresh() {
        autoScrollToTop = true;
        controller.forceUpdate();
    }
}
