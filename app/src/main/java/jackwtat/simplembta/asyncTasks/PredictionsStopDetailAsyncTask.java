package jackwtat.simplembta.asyncTasks;

import jackwtat.simplembta.model.Prediction;

public class PredictionsStopDetailAsyncTask extends PredictionsAsyncTask {
    private String realTimeApiKey;
    private String stopId;
    private OnPostExecuteListener onPostExecuteListener;

    public PredictionsStopDetailAsyncTask(String realTimeApiKey,
                                          String stopId,
                                          OnPostExecuteListener onPostExecuteListener) {
        this.realTimeApiKey = realTimeApiKey;
        this.stopId = stopId;
        this.onPostExecuteListener = onPostExecuteListener;
    }

    @Override
    protected Prediction[] doInBackground(Void... voids) {





        return new Prediction[0];
    }

    @Override
    protected void onPostExecute(Prediction[] predictions) {
        if (predictions != null) {
            onPostExecuteListener.onSuccess(predictions, true);
        } else {
            onPostExecuteListener.onError();
        }
    }
}
