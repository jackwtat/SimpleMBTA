package jackwtat.simplembta.asyncTasks;

import android.os.AsyncTask;

import jackwtat.simplembta.model.Prediction;

public abstract class PredictionsAsyncTask extends AsyncTask<Void, Void, Prediction[]> {
    public interface OnPostExecuteListener {
        void onPostExecute(Prediction[] predictions, boolean live);
    }
}
