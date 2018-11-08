package jackwtat.simplembta.model;

import android.content.Context;

import java.util.HashMap;

import jackwtat.simplembta.R;
import jackwtat.simplembta.utilities.RawResourceReader;
import jackwtat.simplembta.jsonParsers.StopsJsonParser;

public class Stops {
    public static HashMap<String, Stop> greenSubway;
    public static HashMap<String, Stop> greenB;
    public static HashMap<String, Stop> greenC;
    public static HashMap<String, Stop> greenD;
    public static HashMap<String, Stop> greenE;

    public static void init(Context context) {
        greenSubway = getStopsFromJson(context, R.raw.stops_green_subway);
        greenB = getStopsFromJson(context, R.raw.stops_green_b);
        greenC = getStopsFromJson(context, R.raw.stops_green_c);
        greenD = getStopsFromJson(context, R.raw.stops_green_d);
        greenE = getStopsFromJson(context, R.raw.stops_green_e);
    }

    private static HashMap<String, Stop> getStopsFromJson(Context context, int jsonFile) {
        HashMap<String, Stop> stops = StopsJsonParser.parse(RawResourceReader.toString(
                context.getResources().openRawResource(jsonFile)));

        HashMap<String, Stop> childStops = new HashMap<>();

        for (Stop stop : stops.values()) {
            for (String childId : stop.getChildIds()) {
                childStops.put(childId, stop);
            }
        }

        stops.putAll(childStops);

        return stops;
    }
}
