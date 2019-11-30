package jackwtat.simplembta.utilities;

public interface Constants {
    /******************
     ***** TIMING *****
     ******************/
    // Maximum age of prediction
    long MAXIMUM_PREDICTION_AGE = 90000;

    // Predictions auto update rate
    long PREDICTIONS_UPDATE_RATE = 15000;

    // Vehicle locations auto update rate
    long VEHICLES_UPDATE_RATE = 5000;

    // Service alerts auto update rate
    long SERVICE_ALERTS_UPDATE_RATE = 60000;


    /*******************************
     ***** DISTANCE / LOCATION *****
     *******************************/
    // Distance in meters from last target location before target location can be updated
    int DISTANCE_TO_TARGET_LOCATION_UPDATE = 50;

    // Distance in meters from last target location before visible refresh
    int DISTANCE_TO_FORCE_REFRESH = 400;

    // Location auto update rate
    long LOCATION_UPDATE_RATE = 1000;

    // Time between location client updates
    long LOCATION_CLIENT_INTERVAL = 500;

    // Fastest time between location updates
    long FASTEST_LOCATION_CLIENT_INTERVAL = 250;

    // Time since last onStop() before restarting the location
    long LOCATION_UPDATE_RESTART_TIME = 180000;


    /***************
     ***** MAP *****
     ***************/
    // Default level of zoom for the map
    int DEFAULT_MAP_FAR_ZOOM_LEVEL = 13;

    // Default level of zoom for the map
    int DEFAULT_MAP_NEAR_ZOOM_LEVEL = 16;

    // Zoom level where stop markers become visible
    int STOP_MARKER_VISIBILITY_LEVEL = 15;

    // Zoom level where key stop markers become visible
    int KEY_STOP_MARKER_VISIBILITY_LEVEL = 12;

    // Zoom level where commuter rail stop markers become visible
    int COMMUTER_STOP_MARKER_VISIBILITY_LEVEL = 10;
}
