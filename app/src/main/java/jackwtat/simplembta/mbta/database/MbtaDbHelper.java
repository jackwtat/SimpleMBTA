package jackwtat.simplembta.mbta.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import jackwtat.simplembta.mbta.dataNew.Route;
import jackwtat.simplembta.mbta.data.Stop;

import jackwtat.simplembta.mbta.database.MbtaDbContract.StopEntity;
import jackwtat.simplembta.mbta.database.MbtaDbContract.RouteEntity;
import jackwtat.simplembta.mbta.database.MbtaDbContract.StopRouteJoinEntity;

/**
 * Created by jackw on 10/17/2017.
 */

public class MbtaDbHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = "MbtaDbHelper";

    private Context context;

    private static final String DATABASE_NAME = "mbta.db";
    private static final int DATABASE_VERSION = 2;

    private static final String STOPS_CSV_FILE_NAME = "stops.csv";
    private static final long STOP_COUNT = 8308;

    private static final String ROUTES_CSV_FILE_NAME = "routes.csv";
    private static final long ROUTE_COUNT = 215;


    public MbtaDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        loadDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_STOPS_TABLE = "CREATE TABLE " + StopEntity.TABLE_NAME + " ("
                + StopEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + StopEntity.COLUMN_STOP_ID + " TEXT NOT NULL, "
                + StopEntity.COLUMN_STOP_NAME + " TEXT, "
                + StopEntity.COLUMN_STOP_LAT + " REAL, "
                + StopEntity.COLUMN_STOP_LON + " REAL, "
                + StopEntity.COLUMN_PARENT_STATION + " TEXT);";

        String SQL_CREATE_ROUTES_TABLE = "CREATE TABLE " + RouteEntity.TABLE_NAME + " ("
                + RouteEntity._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + RouteEntity.COLUMN_ROUTE_ID + " TEXT NOT NULL, "
                + RouteEntity.COLUMN_SHORT_NAME + " TEXT, "
                + RouteEntity.COLUMN_LONG_NAME + " TEXT, "
                + RouteEntity.COLUMN_COLOR + " TEXT, "
                + RouteEntity.COLUMN_SORT_ORDER + " REAL);";

        // Execute the SQL statements
        db.execSQL(SQL_CREATE_STOPS_TABLE);
        db.execSQL(SQL_CREATE_ROUTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.delete(StopEntity.TABLE_NAME, null, null);

        if (oldVersion >= 2) {
            db.delete(RouteEntity.TABLE_NAME, null, null);
        }
    }

    private void loadDatabase() {
        if (DatabaseUtils.queryNumEntries(getReadableDatabase(), StopEntity.TABLE_NAME) !=
                STOP_COUNT) {
            getWritableDatabase().delete(StopEntity.TABLE_NAME, null, null);
            importStopsFromCsv();
        }

        if (DatabaseUtils.queryNumEntries(getReadableDatabase(), RouteEntity.TABLE_NAME) !=
                ROUTE_COUNT) {
            getWritableDatabase().delete(StopEntity.TABLE_NAME, null, null);
            importRoutesFromCsv();
        }
    }

    // Populate the Stops database with data from stops CSV file
    private void importStopsFromCsv() {
        SQLiteDatabase db = getWritableDatabase();
        String[] csvRecord;

        db.beginTransaction();
        try {
            InputStream inputStream = context.getAssets().open(STOPS_CSV_FILE_NAME);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                csvRecord = line.split(",");

                ContentValues values = new ContentValues();
                values.put(StopEntity.COLUMN_STOP_ID, csvRecord[0]);
                values.put(StopEntity.COLUMN_STOP_NAME, csvRecord[2]);
                values.put(StopEntity.COLUMN_STOP_LAT, csvRecord[4]);
                values.put(StopEntity.COLUMN_STOP_LON, csvRecord[5]);
                values.put(StopEntity.COLUMN_PARENT_STATION, csvRecord[9]);

                db.insert(StopEntity.TABLE_NAME, null, values);
            }
            bufferedReader.close();
            db.setTransactionSuccessful();

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            db.endTransaction();
        }
    }

    // Populate the Routes database with data from routes CSV file
    private void importRoutesFromCsv() {
        SQLiteDatabase db = getWritableDatabase();
        String[] csvRecord;

        db.beginTransaction();
        try {
            InputStream inputStream = context.getAssets().open(ROUTES_CSV_FILE_NAME);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                csvRecord = line.split(",");

                ContentValues values = new ContentValues();
                values.put(RouteEntity.COLUMN_ROUTE_ID, csvRecord[0]);
                values.put(RouteEntity.COLUMN_SHORT_NAME, csvRecord[1]);
                values.put(RouteEntity.COLUMN_LONG_NAME, csvRecord[2]);
                values.put(RouteEntity.COLUMN_COLOR, csvRecord[3]);
                values.put(RouteEntity.COLUMN_SORT_ORDER, csvRecord[4]);

                db.insert(RouteEntity.TABLE_NAME, null, values);
            }
            bufferedReader.close();
            db.setTransactionSuccessful();

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            db.endTransaction();
        }
    }

    // Add
    public void addRoutesByStop(String stopId, List<String> routeIds) {
        SQLiteDatabase db = getReadableDatabase();

        db.beginTransaction();
        try {
            for (String routeId : routeIds) {
                ContentValues values = new ContentValues();

                values.put(StopRouteJoinEntity.COLUMN_STOP_ID, stopId);
                values.put(StopRouteJoinEntity.COLUMN_ROUTE_ID, routeId);

                db.insert(StopRouteJoinEntity.TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
        } catch (IllegalStateException e) {
            e.printStackTrace();

        } finally {
            db.endTransaction();
        }
    }

    public Route getRoute(String routeId) {
        SQLiteDatabase db = getReadableDatabase();

        String whereClause = RouteEntity.COLUMN_ROUTE_ID + "=?";

        String[] whereArgs = {routeId};

        Cursor cursor = db.query(
                RouteEntity.TABLE_NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            int shortNameColumnIndex = cursor.getColumnIndex(RouteEntity.COLUMN_SHORT_NAME);
            int longNameColumnIndex = cursor.getColumnIndex(RouteEntity.COLUMN_LONG_NAME);
            int colorColumnIndex = cursor.getColumnIndex(RouteEntity.COLUMN_COLOR);
            int sortOrderColumnIndex = cursor.getColumnIndex(RouteEntity.COLUMN_SORT_ORDER);

            String shortName = cursor.getString(shortNameColumnIndex);
            String longName = cursor.getString(longNameColumnIndex);
            String color = cursor.getString(colorColumnIndex);
            int sortOrder = cursor.getInt(sortOrderColumnIndex);

            return new Route(routeId, shortName, longName, color, sortOrder);
        } else {
            return null;
        }
    }

    public List<String> getRouteIdsByStop(String stopId) {
        SQLiteDatabase db = getReadableDatabase();

        List<String> routeIds = new ArrayList<>();

        String whereClause = StopRouteJoinEntity.COLUMN_STOP_ID + "=?";

        String[] whereArgs = {stopId};

        Cursor cursor = db.query(
                StopRouteJoinEntity.TABLE_NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        int routeIdColumnIndex = cursor.getColumnIndex(StopRouteJoinEntity.COLUMN_ROUTE_ID);

        while (cursor.moveToNext()) {
            routeIds.add(cursor.getString(routeIdColumnIndex));
        }

        return routeIds;
    }

    public List<Stop> getStopsByLocation(Location location, double maxDistance) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Stop> stops = new ArrayList<>();

        final double milesPerLat = 69;
        final double milesPerLon = 69.172;

        double lat = location.getLatitude();
        double lon = location.getLongitude();

        double minLat = lat - maxDistance / milesPerLat;
        double maxLat = lat + maxDistance / milesPerLat;
        double minLon = lon - maxDistance / (milesPerLon * Math.cos(Math.toRadians(lat)));
        double maxLon = lon + maxDistance / (milesPerLon * Math.cos(Math.toRadians(lat)));

        String whereClause = StopEntity.COLUMN_STOP_LAT + ">? AND " +
                StopEntity.COLUMN_STOP_LAT + "<? AND " +
                StopEntity.COLUMN_STOP_LON + ">? AND " +
                StopEntity.COLUMN_STOP_LON + "<?";

        String[] whereArgs = {
                Double.toString(minLat),
                Double.toString(maxLat),
                Double.toString(minLon),
                Double.toString(maxLon)};

        Cursor cursor = db.query(
                StopEntity.TABLE_NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        int stopIdColumnIndex = cursor.getColumnIndex(StopEntity.COLUMN_STOP_ID);
        int stopNameColumnIndex = cursor.getColumnIndex(StopEntity.COLUMN_STOP_NAME);
        int stopParentColumnIndex = cursor.getColumnIndex(StopEntity.COLUMN_PARENT_STATION);
        int stopLatColumnIndex = cursor.getColumnIndex(StopEntity.COLUMN_STOP_LAT);
        int stopLonColumnIndex = cursor.getColumnIndex(StopEntity.COLUMN_STOP_LON);

        while (cursor.moveToNext()) {
            // Get stop data
            String stopId = cursor.getString(stopIdColumnIndex);
            String stopName = cursor.getString(stopNameColumnIndex);
            String stopParentId = cursor.getString(stopParentColumnIndex);
            Double stopLat = cursor.getDouble(stopLatColumnIndex);
            Double stopLon = cursor.getDouble(stopLonColumnIndex);

            // Calculate distance between stop and device locations using the pythagorean theorem
            Double stopDistance = Math.sqrt(Math.pow((stopLat - lat) * milesPerLat, 2) +
                    Math.pow((stopLon - lon) * milesPerLon, 2));

            // Create new instance of StopEntity
            Stop newStop = new Stop(stopId, stopName, stopLat, stopLon, stopDistance);

            // If distance is less than max distance and list does not already contain stop
            if (stopDistance <= maxDistance) {
                if (stopParentId.equals("")) {
                    stops.add(newStop);

                } else {
                    Stop parentStop = getStopById(stopParentId);

                    if (!stops.contains(parentStop)) {
                        parentStop.setDistance(stopDistance);
                        stops.add(parentStop);
                    }
                }
            }
        }

        cursor.close();

        return stops;
    }

    public Stop getStopById(String stopId) {
        SQLiteDatabase db = getReadableDatabase();

        String whereClause = MbtaDbContract.StopEntity.COLUMN_STOP_ID + "=?";
        String[] whereArgs = {stopId};

        Cursor cursor = db.query(
                StopEntity.TABLE_NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        int stopNameColumnIndex = cursor.getColumnIndex(StopEntity.COLUMN_STOP_NAME);
        int stopLatColumnIndex = cursor.getColumnIndex(StopEntity.COLUMN_STOP_LAT);
        int stopLonColumnIndex = cursor.getColumnIndex(StopEntity.COLUMN_STOP_LON);

        cursor.moveToFirst();

        String stopName = cursor.getString(stopNameColumnIndex);
        Double stopLat = cursor.getDouble(stopLatColumnIndex);
        Double stopLon = cursor.getDouble(stopLonColumnIndex);

        return new Stop(stopId, stopName, stopLat, stopLon);
    }
}
