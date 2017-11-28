package jackwtat.simplembta.mbta.database;

import android.provider.BaseColumns;

/**
 * Created by jackw on 10/18/2017.
 */

public final class MbtaDbContract {
    private MbtaDbContract() {}

    public static final class StopEntity implements BaseColumns {

        public final static String TABLE_NAME = "stops";

        /**
         * Unique ID number for the stop (only for use in the database table)
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Unique ID for the stop
         *
         * Type: TEXT
         */
        public final static String COLUMN_STOP_ID = "stop_id";

        /**
         * Name of the stop
         *
         * Type: TEXT
         */
        public final static String COLUMN_STOP_NAME = "stop_name";

        /**
         * Latitudinal coordinate of the stop
         *
         * Type: FLOAT(64)
         */
        public final static String COLUMN_STOP_LAT = "stop_lat";

        /**
         * Longitudinal coordinate of the stop
         *
         * Type: FLOAT(64)
         */
        public final static String COLUMN_STOP_LON = "stop_lon";

        /**
         * stop_id of the parent station, if one exists
         *
         * Type: TEXT
         */
        public final static String COLUMN_PARENT_STATION = "parent_station";
    }

    public static final class RouteEntity implements BaseColumns {

        public final static String TABLE_NAME = "routes";

        /**
         * Unique ID number for the stop (only for use in the database table)
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Unique ID for the route
         *
         * Type: TEXT
         */
        public final static String COLUMN_ROUTE_ID = "route_id";

        /**
         * The short name of the route
         *
         * Type: TEXT
         */
        public final static String COLUMN_SHORT_NAME = "short_name";

        /**
         * The long name of the route
         *
         * Type: TEXT
         */
        public final static String COLUMN_LONG_NAME = "long_name";

        /**
         * The hex color code for the route's branding
         *
         * Type: TEXT
         */
        public final static String COLUMN_COLOR = "color";

        /**
         * A ranking that helps with sorting routes
         *
         * Type: INTEGER
         */
        public final static String COLUMN_SORT_ORDER = "sort_order";

    }

    public static final class StopRouteJoinEntity implements BaseColumns{

        public final static String TABLE_NAME = "stop_route_joins";

        /**
         * Unique ID number for the stop (only for use in the database table)
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * ID for the stop
         *
         * Type: TEXT
         */
        public final static String COLUMN_STOP_ID = "stop_id";

        /**
         * ID for the route
         *
         * Type: TEXT
         */
        public final static String COLUMN_ROUTE_ID = "route_id";
    }
}
