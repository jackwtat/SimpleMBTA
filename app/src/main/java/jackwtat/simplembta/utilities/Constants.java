package jackwtat.simplembta.utilities;

public interface Constants {
    /******************
     ***** TIMING *****
     ******************/
    // Maximum age of prediction
    long MAXIMUM_PREDICTION_AGE = 90000;

    // Predictions auto update rate
    long PREDICTIONS_UPDATE_RATE = 40000;

    // Vehicle locations auto update rate
    long VEHICLES_UPDATE_RATE = 5000;

    // Service alerts auto update rate
    long SERVICE_ALERTS_UPDATE_RATE = 60000;

    // Location auto update rate
    long LOCATION_UPDATE_RATE = 1000;

    // Time between location client updates
    long LOCATION_CLIENT_INTERVAL = 500;

    // Fastest time between location updates
    long FASTEST_LOCATION_CLIENT_INTERVAL = 250;

    // Time since last onStop() before restarting the location
    long LOCATION_UPDATE_RESTART_TIME = 180000;

    long COUNTDOWN_HOUR_CUTOFF = 3600000;

    // Cutoff countdown time to display 'Approaching' message
    long COUNTDOWN_APPROACHING_CUTOFF = 60000;

    // Cutoff countdown time to display 'Arriving' message
    long COUNTDOWN_ARRIVING_CUTOFF = 20000;

    // Cutoff countdown time to display 'Departing' message
    long COUNTDOWN_DEPARTING_CUTOFF = 0;


    /*******************************
     ***** DISTANCE / LOCATION *****
     *******************************/
    // Distance in meters from last target location before target location can be updated
    int DISTANCE_TO_TARGET_LOCATION_UPDATE = 50;

    // Distance in meters from last target location before visible refresh
    int DISTANCE_TO_FORCE_REFRESH = 400;

    // Quarter mile search distance
    double SEARCH_DISTANCE_QUARTER_MILE = 0.005;

    // Half mile search distance
    double SEARCH_DISTANCE_HALF_MILE = 0.01;

    // One mile search distance
    double SEARCH_DISTANCE_ONE_MILE = 0.02;

    // MBTA service boundary coordinates
    double NORTH_LATITUDE_BOUNDARY = 42.9;
    double SOUTH_LATITUDE_BOUNDARY = 41.3;
    double WEST_LONGITUDE_BOUNDARY = -71.9;
    double EAST_LONGITUDE_BOUNDARY = -69.9;


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
