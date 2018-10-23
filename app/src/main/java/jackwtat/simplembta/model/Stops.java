package jackwtat.simplembta.model;

import android.content.Context;

import java.util.HashMap;

import jackwtat.simplembta.R;
import jackwtat.simplembta.utilities.RawResourceReader;
import jackwtat.simplembta.utilities.StopsJsonParser;

public class Stops {
    public static HashMap<String, Stop> greenLineSubway;
    public static HashMap<String, Stop> greenLineB;
    public static HashMap<String, Stop> greenLineC;
    public static HashMap<String, Stop> greenLineD;
    public static HashMap<String, Stop> greenLineE;

    public static void init(Context context) {
        greenLineSubway = getStopsFromJson(context, R.raw.stops_green_subway);
        greenLineB = getStopsFromJson(context, R.raw.stops_green_b);
        greenLineC = getStopsFromJson(context, R.raw.stops_green_c);
        greenLineD = getStopsFromJson(context, R.raw.stops_green_d);
        greenLineE = getStopsFromJson(context, R.raw.stops_green_e);
    }

    private static HashMap<String, Stop> getStopsFromJson(Context context, int jsonFile) {
        return StopsJsonParser.parse(RawResourceReader.toString(
                context.getResources().openRawResource(jsonFile)));
    }
}
