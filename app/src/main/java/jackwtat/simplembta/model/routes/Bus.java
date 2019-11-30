package jackwtat.simplembta.model.routes;

import jackwtat.simplembta.map.BusStopMarkerFactory;
import jackwtat.simplembta.model.Route;

public class Bus extends Route {
    public Bus(String id) {
        super(id);
        setPrimaryColor("ffc72c");
        setTextColor("000000");
        setStopMarkerFactory(new BusStopMarkerFactory());
    }

    public static boolean isParentOf(String parentId, String childId) {
        if (parentId.equals("2427") && (childId.equals("24") || childId.equals("27")))
            return true;
        else if (parentId.equals("3233") && (childId.equals("32") || childId.equals("33")))
            return true;
        else if (parentId.equals("3738") && (childId.equals("37") || childId.equals("38")))
            return true;
        else if (parentId.equals("4050") && (childId.equals("40") || childId.equals("50")))
            return true;
        else if (parentId.equals("627") && (childId.equals("62") || childId.equals("76")))
            return true;
        else if (parentId.equals("725") && (childId.equals("72") || childId.equals("75")))
            return true;
        else if (parentId.equals("8993") && (childId.equals("89") || childId.equals("93")))
            return true;
        else if (parentId.equals("116117") && (childId.equals("116") || childId.equals("117")))
            return true;
        else if (parentId.equals("214216") && (childId.equals("214") || childId.equals("216")))
            return true;
        else if (parentId.equals("441442") && (childId.equals("441") || childId.equals("442")))
            return true;
        else return false;
    }
}
