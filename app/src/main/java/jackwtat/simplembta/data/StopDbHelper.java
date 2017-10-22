package jackwtat.simplembta.data;

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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import jackwtat.simplembta.data.StopContract.StopEntry;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

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
        db.delete(StopEntry.TABLE_NAME, null, null);
    }

    public void loadDatabase(Context context) {
        if (DatabaseUtils.queryNumEntries(getReadableDatabase(), StopEntry.TABLE_NAME) != STOP_COUNT) {
            Log.i(LOG_TAG, "Loading database");
            getWritableDatabase().delete(StopEntry.TABLE_NAME, null, null);
            importFromCsv(context, CSV_FILE_NAME);
        }
    }

    public void importFromCsv(Context context, String csvFile) {
        SQLiteDatabase db = getWritableDatabase();
        List<String[]> csvLine = new ArrayList<>();
        String[] csvRecord;

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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Stop> getStopsByLocation(Location location, double maxDistance) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Stop> stops = new ArrayList<>();

        double lat = location.getLatitude();
        double lon = location.getLongitude();

        double minLat = lat - maxDistance / 69;
        double maxLat = lat + maxDistance / 69;
        double minLon = lon - maxDistance / (69.172 * Math.cos(Math.toRadians(lat)));
        double maxLon = lon + maxDistance / (69.172 * Math.cos(Math.toRadians(lat)));

        Log.i(LOG_TAG, "minLat=" + minLat);
        Log.i(LOG_TAG, "maxLat=" + maxLat);
        Log.i(LOG_TAG, "minLon=" + minLon);
        Log.i(LOG_TAG, "maxLon=" + maxLon);

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
        int stopLatColumnIndex = cursor.getColumnIndex(StopEntry.COLUMN_STOP_LAT);
        int stopLonColumnIndex = cursor.getColumnIndex(StopEntry.COLUMN_STOP_LON);

        while (cursor.moveToNext()) {
            String stopId = cursor.getString(stopIdColumnIndex);
            String stopName = cursor.getString(stopNameColumnIndex);
            Double stopLat = cursor.getDouble(stopLatColumnIndex);
            Double stopLon = cursor.getDouble(stopLonColumnIndex);
            Double stopDistance = Math.sqrt(Math.pow(stopLat - lat, 2) + Math.pow(stopLon - lon, 2));

            if (stopDistance <= maxDistance) {
                stops.add(new Stop(stopId, stopName, stopLat, stopLat, stopDistance));
            }
        }

        cursor.close();

        return stops;
    }
}
