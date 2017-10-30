package jackwtat.simplembta;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import jackwtat.simplembta.data.Stop;
import jackwtat.simplembta.StopContract.StopEntry;

/**
 * Created by jackw on 10/17/2017.
 */

public class StopDbHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = "StopDbHelper";

    private static final String CSV_FILE_NAME = "stops.csv";
    private static final String DATABASE_NAME = "stops.db";
    private static final long STOP_COUNT = 8308;
    private static final int DATABASE_VERSION = 1;

    public StopDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_STOPS_TABLE = "CREATE TABLE " + StopEntry.TABLE_NAME + " ("
                + StopEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + StopEntry.COLUMN_STOP_ID + " TEXT NOT NULL, "
                + StopEntry.COLUMN_STOP_NAME + " TEXT, "
                + StopEntry.COLUMN_STOP_LAT + " REAL, "
                + StopEntry.COLUMN_STOP_LON + " REAL, "
                + StopEntry.COLUMN_PARENT_STATION + " TEXT);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_STOPS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.delete(StopContract.StopEntry.TABLE_NAME, null, null);
    }

    public void loadDatabase(Context context) {
        if (DatabaseUtils.queryNumEntries(getReadableDatabase(), StopEntry.TABLE_NAME) != STOP_COUNT) {
            getWritableDatabase().delete(StopEntry.TABLE_NAME, null, null);
            importFromCsv(context, CSV_FILE_NAME);
        }
    }

    public void importFromCsv(Context context, String csvFile) {
        SQLiteDatabase db = getWritableDatabase();
        List<String[]> csvLine = new ArrayList<>();
        String[] csvRecord;

        db.beginTransaction();
        try {
            InputStream inputStream = context.getAssets().open(csvFile);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                csvRecord = line.split(",");
                csvLine.add(csvRecord);

                ContentValues values = new ContentValues();
                values.put(StopEntry.COLUMN_STOP_ID, csvRecord[0]);
                values.put(StopEntry.COLUMN_STOP_NAME, csvRecord[2]);
                values.put(StopEntry.COLUMN_STOP_LAT, csvRecord[4]);
                values.put(StopEntry.COLUMN_STOP_LON, csvRecord[5]);
                values.put(StopEntry.COLUMN_PARENT_STATION, csvRecord[9]);

                db.insert(StopEntry.TABLE_NAME, null, values);
            }
            bufferedReader.close();
            db.setTransactionSuccessful();

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            db.endTransaction();
        }
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

        String whereClause = StopEntry.COLUMN_STOP_LAT + ">? AND " +
                StopEntry.COLUMN_STOP_LAT + "<? AND " +
                StopEntry.COLUMN_STOP_LON + ">? AND " +
                StopEntry.COLUMN_STOP_LON + "<?";

        String[] whereArgs = {
                Double.toString(minLat),
                Double.toString(maxLat),
                Double.toString(minLon),
                Double.toString(maxLon)};

        Cursor cursor = db.query(
                StopEntry.TABLE_NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        int stopIdColumnIndex = cursor.getColumnIndex(StopEntry.COLUMN_STOP_ID);
        int stopNameColumnIndex = cursor.getColumnIndex(StopEntry.COLUMN_STOP_NAME);
        int stopParentColumnIndex = cursor.getColumnIndex(StopEntry.COLUMN_PARENT_STATION);
        int stopLatColumnIndex = cursor.getColumnIndex(StopEntry.COLUMN_STOP_LAT);
        int stopLonColumnIndex = cursor.getColumnIndex(StopEntry.COLUMN_STOP_LON);

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

            // Create new instance of Stop
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

    public Stop getStopById(String id) {
        SQLiteDatabase db = getReadableDatabase();

        String whereClause = StopEntry.COLUMN_STOP_ID + "=?";
        String[] whereArgs = {id};

        Cursor cursor = db.query(
                StopEntry.TABLE_NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        int stopIdColumnIndex = cursor.getColumnIndex(StopEntry.COLUMN_STOP_ID);
        int stopNameColumnIndex = cursor.getColumnIndex(StopEntry.COLUMN_STOP_NAME);
        int stopLatColumnIndex = cursor.getColumnIndex(StopEntry.COLUMN_STOP_LAT);
        int stopLonColumnIndex = cursor.getColumnIndex(StopEntry.COLUMN_STOP_LON);

        cursor.moveToFirst();

        String stopId = cursor.getString(stopIdColumnIndex);
        String stopName = cursor.getString(stopNameColumnIndex);
        Double stopLat = cursor.getDouble(stopLatColumnIndex);
        Double stopLon = cursor.getDouble(stopLonColumnIndex);

        return new Stop(stopId, stopName, stopLat, stopLon);
    }
}
