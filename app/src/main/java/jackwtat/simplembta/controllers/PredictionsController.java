package jackwtat.simplembta.controllers;

/**
 * Created by jackw on 3/24/2018.
 */

public interface PredictionsController {
    void connect();

    void disconnect();

    void update();

    void forceUpdate();

    void cancel();

    boolean isRunning();

    long getTimeSinceLastRefresh();
}
