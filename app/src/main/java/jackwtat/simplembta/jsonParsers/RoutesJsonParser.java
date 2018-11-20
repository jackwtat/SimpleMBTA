package jackwtat.simplembta.jsonParsers;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import jackwtat.simplembta.model.Direction;
import jackwtat.simplembta.model.routes.BlueLine;
import jackwtat.simplembta.model.routes.Bus;
import jackwtat.simplembta.model.routes.CommuterRail;
import jackwtat.simplembta.model.routes.Ferry;
import jackwtat.simplembta.model.routes.GreenLine;
import jackwtat.simplembta.model.routes.OrangeLine;
import jackwtat.simplembta.model.routes.RedLine;
import jackwtat.simplembta.model.routes.Route;
import jackwtat.simplembta.model.routes.SilverLine;

public class RoutesJsonParser {
    public static final String LOG_TAG = "RoutesJsonParser";

    public static Route[] parse(String jsonResponse) {
        if (TextUtils.isEmpty(jsonResponse)) {
            return new Route[0];
        }

        ArrayList<Route> routes = new ArrayList<>();

        try {
            JSONObject jRoot = new JSONObject(jsonResponse);

            JSONArray jData = jRoot.getJSONArray("data");

            for (int i = 0; i < jData.length(); i++) {
                try {
                    JSONObject jRoute = jData.getJSONObject(i);

                    String id = jRoute.getString("id");

                    JSONObject jAttributes = jRoute.getJSONObject("attributes");
                    int mode = jAttributes.getInt("type");
                    int sortOrder = jAttributes.getInt("sort_order");
                    String shortName = jAttributes.getString("short_name");
                    String longName = jAttributes.getString("long_name");
                    String primaryColor = jAttributes.getString("color");
                    String textColor = jAttributes.getString("text_color");

                    Route route;

                    if (mode == Route.BUS) {
                        if (SilverLine.isSilverLine(id)) {
                            route = new SilverLine(id);
                        } else {
                            route = new Bus(id);
                        }
                    } else if (mode == Route.HEAVY_RAIL) {
                        if (BlueLine.isBlueLine(id)) {
                            route = new BlueLine(id);
                        } else if (OrangeLine.isOrangeLine(id)) {
                            route = new OrangeLine(id);
                        } else if (RedLine.isRedLine(id)) {
                            route = new RedLine(id);
                        } else {
                            route = new Route(id);
                        }
                    } else if (mode == Route.LIGHT_RAIL) {
                        if (GreenLine.isGreenLine(id)) {
                            route = new GreenLine(id);
                        } else if (RedLine.isRedLine(id)) {
                            route = new RedLine(id);
                        } else {
                            route = new Route(id);
                        }
                    } else if (mode == Route.COMMUTER_RAIL) {
                        route = new CommuterRail(id);
                    } else if (mode == Route.FERRY) {
                        route = new Ferry(id);
                    } else {
                        route = new Route(id);
                    }

                    route.setMode(mode);
                    route.setSortOrder(sortOrder);
                    route.setShortName(shortName);
                    route.setLongName(longName);
                    route.setPrimaryColor(primaryColor);
                    route.setTextColor(textColor);

                    JSONArray jDirectionNames = jAttributes.getJSONArray("direction_names");
                    for (int j = 0; j < jDirectionNames.length(); j++) {
                        route.setDirection(new Direction(j, jDirectionNames.getString(j)));

                    }

                    routes.add(route);

                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Unable to parse Route at position " + i);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse Routes JSON response");
        }

        return routes.toArray(new Route[routes.size()]);
    }
}
