package jackwtat.simplembta.utilities;

import android.content.Context;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.model.routes.SilverLine;

public class DisplayNameUtil {
    // Returns the language-specific short name of this route
    // Context is required to get the proper translation
    public static String getShortDisplayName(Context context, Route route) {
        if (route.getMode() == Route.HEAVY_RAIL) {
            if (route.getId().equals("Red"))
                return context.getResources().getString(R.string.red_line_short_name);
            else if (route.getId().equals("Orange"))
                return context.getResources().getString(R.string.orange_line_short_name);
            else if (route.getId().equals("Blue"))
                return context.getResources().getString(R.string.blue_line_short_name);
            else
                return route.getId();

        } else if (route.getMode() == Route.LIGHT_RAIL) {
            if (route.getId().equals("Green-B"))
                return context.getResources().getString(R.string.green_line_b_short_name);
            else if (route.getId().equals("Green-C"))
                return context.getResources().getString(R.string.green_line_c_short_name);
            else if (route.getId().equals("Green-D"))
                return context.getResources().getString(R.string.green_line_d_short_name);
            else if (route.getId().equals("Green-E"))
                return context.getResources().getString(R.string.green_line_e_short_name);
            else if (route.getId().equals("Mattapan"))
                return context.getResources().getString(R.string.red_line_mattapan_short_name);
            else
                return route.getId();

        } else if (route.getMode() == Route.BUS) {
            if (route.getId().equals("746"))
                return context.getResources().getString(R.string.silver_line_waterfront_short_name);
            else if (!route.getShortName().equals("") && !route.getShortName().equals("null"))
                return route.getShortName();
            else
                return route.getId();

        } else if (route.getMode() == Route.COMMUTER_RAIL) {
            if (route.getId().equals("CapeFlyer")) {
                return context.getResources().getString(R.string.cape_flyer_short_name);
            } else {
                return context.getResources().getString(R.string.commuter_rail_short_name);
            }

        } else if (route.getMode() == Route.FERRY) {
            return context.getResources().getString(R.string.ferry_short_name);

        } else {
            return route.getId();
        }
    }

    // Returns the language-specific full name of this route
    // Context is required to get the proper translation
    public static String getLongDisplayName(Context context, Route route) {
        if (route.getMode() == Route.BUS) {
            if (SilverLine.isSilverLine(route.getId())) {
                return context.getResources().getString((R.string.silver_line_long_name)) +
                        " " + route.getShortName();
            } else {
                return context.getResources().getString(R.string.route_prefix) +
                        " " + route.getShortName();
            }
        } else if (!route.getLongName().equals("") && !route.getLongName().equals("null")) {
            return route.getLongName();
        } else {
            return route.getId();
        }
    }
}
