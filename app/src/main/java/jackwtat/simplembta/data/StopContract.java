package jackwtat.simplembta.data;

import android.provider.BaseColumns;

/**
 * Created by jackw on 10/18/2017.
 */

public final class StopContract {
    private StopContract() {}

    public static final class StopEntry implements BaseColumns {

        public final static String TABLE_NAME = "stops";

        /**
         * Unique ID number for the stop (only for use in the database table)
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Unique ID for the stop. (for use all other uses including querying from realTime API)
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
}
