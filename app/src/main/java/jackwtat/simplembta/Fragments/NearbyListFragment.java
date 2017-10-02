package jackwtat.simplembta.Fragments;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import jackwtat.simplembta.MbtaData.Stop;
import jackwtat.simplembta.Utils.QueryUtil;

/**
 * Created by jackw on 9/30/2017.
 */

public class NearbyListFragment extends PredictionsListFragment implements ConnectionCallbacks, OnConnectionFailedListener{
    private final String TAG = "NearbyListFragment";

    private int REQUEST_ACCESS_FINE_LOCATION = 1;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private double mLatitude;
    private double mLongitude;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected List<Stop> getStops() {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class PredictionAsyncTask extends AsyncTask<Stop, Void, List<Stop>>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Stop> doInBackground(Stop... params) {
            if (params.length < 1 || params[0] == null){
                return null;
            }

            ArrayList<Stop> results = new ArrayList<>();

            for (Stop stop : params){
                String id = stop.getId();

                stop.addRoutes(QueryUtil.fetchRoutesByStop(id));
                stop.addPredictions(QueryUtil.fetchPredictionsByStop(id));

                results.add(stop);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Stop> stops) {
            populateList(stops);
        }
    }
}
