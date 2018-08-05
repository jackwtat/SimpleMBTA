package jackwtat.simplembta;

import java.util.ArrayList;

public class ErrorMessageHandler {
    private static ErrorMessageHandler errorMessageHandler;

    private static ArrayList<OnErrorChangedListener> onErrorChangedListeners = new ArrayList<>();
    private static boolean networkError = false;
    private static boolean locationError = false;
    private static boolean locationPermissionDenied = false;

    private ErrorMessageHandler() {
    }

    public static synchronized ErrorMessageHandler getErrorMessageHandler() {
        if (errorMessageHandler == null) {
            errorMessageHandler = new ErrorMessageHandler();
        }
        return errorMessageHandler;
    }

    public void registerOnErrorChangeListener(OnErrorChangedListener listener) {
        onErrorChangedListeners.add(listener);
    }

    private void notifyErrorChanged() {
        for (OnErrorChangedListener listener : onErrorChangedListeners) {
            listener.onErrorChanged();
        }
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

    public interface OnErrorChangedListener {
        void onErrorChanged();
    }
}
