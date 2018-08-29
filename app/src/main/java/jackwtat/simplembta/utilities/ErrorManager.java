package jackwtat.simplembta.utilities;

import java.util.ArrayList;

public class ErrorManager {
    private static ErrorManager errorManager;

    private static ArrayList<OnErrorChangedListener> onErrorChangedListeners = new ArrayList<>();
    private static boolean networkError = false;
    private static boolean locationError = false;
    private static boolean locationPermissionDenied = false;

    private ErrorManager() {
    }

    public static synchronized ErrorManager getErrorManager() {
        if (errorManager == null) {
            errorManager = new ErrorManager();
        }
        return errorManager;
    }

    public void setNetworkError(boolean error) {
        networkError = error;
        notifyErrorChanged();
    }

    public void setLocationError(boolean error) {
        locationError = error;
        notifyErrorChanged();
    }

    public void setLocationPermissionDenied(boolean error) {
        locationPermissionDenied = error;
        notifyErrorChanged();
    }

    public boolean hasNetworkError() {
        return networkError;
    }

    public boolean hasLocationError() {
        return locationError;
    }

    public boolean hasLocationPermissionDenied() {
        return locationPermissionDenied;
    }

    public void registerOnErrorChangeListener(OnErrorChangedListener listener) {
        onErrorChangedListeners.add(listener);
    }

    private void notifyErrorChanged() {
        for (OnErrorChangedListener listener : onErrorChangedListeners) {
            listener.onErrorChanged();
        }
    }

    public interface OnErrorChangedListener {
        void onErrorChanged();
    }
}
