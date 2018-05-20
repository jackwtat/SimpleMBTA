package jackwtat.simplembta.controllers.listeners;

import java.util.List;

import jackwtat.simplembta.mbta.structure.Stop;

/**
 * Created by jackw on 3/25/2018.
 */

public interface OnPostExecuteListener {
    void onPostExecute(List<Stop> stops);
}
