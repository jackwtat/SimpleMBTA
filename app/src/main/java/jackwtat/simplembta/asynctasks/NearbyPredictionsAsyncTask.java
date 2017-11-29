package jackwtat.simplembta.asynctasks;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jackwtat.simplembta.R;
import jackwtat.simplembta.mbta.api.PredictionsByStopQuery;
import jackwtat.simplembta.mbta.api.RealTimeApi;
import jackwtat.simplembta.mbta.api.ServiceAlertsQuery;
import jackwtat.simplembta.mbta.data.ServiceAlert;
import jackwtat.simplembta.mbta.data.Stop;
import jackwtat.simplembta.mbta.data.Trip;
import jackwtat.simplembta.mbta.database.MbtaDbHelper;

/**
 * Created by jackw on 11/29/2017.
 */

public class NearbyPredictionsAsyncTask extends AsyncTask<Location, Integer, List<Stop>> {
    private RealTimeApi rt;
    private MbtaDbHelper db;
    private double maxDistance;
    private OnProgressUpdateListener onProgressUpdateListeners;
    private OnPostExecuteListener onPostExecuteListeners;

    public NearbyPredictionsAsyncTask(RealTimeApi realTimeApi, MbtaDbHelper mbtaDbHelper, double maxDistance) {
        this.rt = realTimeApi;
        this.db = mbtaDbHelper;
        this.maxDistance = maxDistance;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected List<Stop> doInBackground(Location... locations) {

        // Get all stops within the specified maximum distance from user's location
        List<Stop> stops = db.getStopsByLocation(locations[0], maxDistance);

        // Get all service alerts
        HashMap<String, ArrayList<ServiceAlert>> alerts = new ServiceAlertsQuery(rt).get();

        // Get predicted trips for each stop
        PredictionsByStopQuery pbsQuery = new PredictionsByStopQuery(rt);
        for (int i = 0; i < stops.size(); i++) {
            Stop stop = stops.get(i);

            stop.addTrips(pbsQuery.get(stop.getId()));

            // Add alerts to trips whose route has alerts
            for (Trip trip : stop.getTrips()) {
                if (alerts.containsKey(trip.getRouteId())) {
                    trip.setAlerts(alerts.get(trip.getRouteId()));
                }
            }

            publishProgress((int) (100 * (i + 1) / stops.size()));
        }

        return stops;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        onProgressUpdateListeners.onProgressUpdate(progress[0]);
    }

    @Override
    protected void onPostExecute(List<Stop> stops) {
        onPostExecuteListeners.onPostExecute(stops);
    }

    public void setOnPostExecuteListener(OnPostExecuteListener listener) {
        onPostExecuteListeners = listener;
    }

    public void setOnProgressUpdateListener(OnProgressUpdateListener listener) {
        onProgressUpdateListeners = listener;
    }

    public interface OnProgressUpdateListener {
        void onProgressUpdate(int progress);
    }

    public interface OnPostExecuteListener {
        void onPostExecute(List<Stop> stops);
    }
}
