package jackwtat.simplembta.model.routes;

import jackwtat.simplembta.map.GreenLineStopMarkerFactory;
import jackwtat.simplembta.model.Direction;
import jackwtat.simplembta.model.Prediction;

public class GreenLine extends RailRoute {


    public GreenLine(String id) {
        super(id);
        setPrimaryColor("00843D");
        setTextColor("FFFFFF");
        setStopMarkerFactory(new GreenLineStopMarkerFactory());
    }

    @Override
    public void addPrediction(Prediction prediction) {
        // Out of service Green Line trains sometimes show up as in service on incorrect lines
        // if they're moving with their AVI turned on
        if ((isCorrectGreenLineStop(prediction.getRouteId(), prediction.getStopId()) ||
                isCorrectGreenLineStop(prediction.getRouteId(), prediction.getParentStopId())) &&
                isValidDestination(prediction.getDirection(), prediction.getStopId(), prediction.getDestination())) {
            super.addPrediction(prediction);
        } else {
            if (getFocusStop(0) != null &&
                    getFocusStop(0).equals(prediction.getStop())) {
                setFocusStop(0, null);
            }
            if (getFocusStop(1) != null &&
                    getFocusStop(1).equals(prediction.getStop())) {
                setFocusStop(1, null);
            }
        }
    }

    public static boolean isGreenLine(String routeId) {
        return isGreenLineB(routeId) ||
                isGreenLineC(routeId) ||
                isGreenLineD(routeId) ||
                isGreenLineE(routeId);
    }

    public static boolean isGreenLineB(String routeId) {
        return routeId.contains("Green-B");
    }

    public static boolean isGreenLineC(String routeId) {
        return routeId.contains("Green-C");
    }

    public static boolean isGreenLineD(String routeId) {
        return routeId.contains("Green-D");
    }

    public static boolean isGreenLineE(String routeId) {
        return routeId.contains("Green-E");
    }

    public static boolean isCorrectGreenLineStop(String routeId, String stopId) {
        if (isGreenLineB(routeId) && (isGreenLineSubwayStop(stopId) || isGreenLineBStop(stopId))) {
            return true;
        } else if (isGreenLineC(routeId) && (isGreenLineSubwayStop(stopId) || isGreenLineCStop(stopId))) {
            return true;
        } else if (isGreenLineD(routeId) && (isGreenLineSubwayStop(stopId) || isGreenLineDStop(stopId))) {
            return true;
        } else if (isGreenLineE(routeId) && (isGreenLineSubwayStop(stopId) || isGreenLineEStop(stopId))) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isValidDestination(int direction, String originStopId,
                                             String destinationStopName) {
        if (direction == Direction.EASTBOUND) {
            if ((originStopId.equals("place-lech") || originStopId.equals("place-spmnl")) &&
                    (destinationStopName.equals("North Station") ||
                            destinationStopName.equals("Haymarket") ||
                            destinationStopName.equals("Government Center") ||
                            destinationStopName.equals("Park Street"))) {
                return false;

            } else if ((originStopId.equals("place-north") || originStopId.equals("place-haecl")) &&
                    (destinationStopName.equals("Government Center") ||
                            destinationStopName.equals("Park Street"))) {
                return false;

            } else if (originStopId.equals("place-gover") &&
                    destinationStopName.equals("Park Street")) {
                return false;

            } else {
                return true;
            }
        } else {
            if ((originStopId.equals("place-hymnl") || originStopId.equals("place-kencl")) &&
                    (destinationStopName.equals("Heath Street") ||
                            destinationStopName.equals("Brigham Circle") ||
                            destinationStopName.equals("Northeastern University"))) {
                return false;

            } else {
                return true;
            }
        }
    }

    public static boolean isGreenLineSubwayStop(String stopId) {
        return stopId.equals("place-lech") ||
                stopId.equals("place-spmnl") ||
                stopId.equals("place-north") ||
                stopId.equals("place-haecl") ||
                stopId.equals("place-gover") ||
                stopId.equals("place-pktrm") ||
                stopId.equals("place-boyls") ||
                stopId.equals("place-armnl") ||
                stopId.equals("place-coecl") ||
                stopId.equals("place-hymnl") ||
                stopId.equals("place-kencl");
    }

    public static boolean isGreenLineBStop(String stopId) {
        return stopId.equals("place-lake") ||
                stopId.equals("place-sougr") ||
                stopId.equals("place-chill") ||
                stopId.equals("place-chswk") ||
                stopId.equals("place-sthld") ||
                stopId.equals("place-wascm") ||
                stopId.equals("place-wrnst") ||
                stopId.equals("place-alsgr") ||
                stopId.equals("place-grigg") ||
                stopId.equals("place-harvd") ||
                stopId.equals("place-brico") ||
                stopId.equals("place-babck") ||
                stopId.equals("place-plsgr") ||
                stopId.equals("place-stplb") ||
                stopId.equals("place-buwst") ||
                stopId.equals("place-bucen") ||
                stopId.equals("place-buest") ||
                stopId.equals("place-bland");
    }

    public static boolean isGreenLineCStop(String stopId) {
        return stopId.equals("place-clmnl") ||
                stopId.equals("place-engav") ||
                stopId.equals("place-denrd") ||
                stopId.equals("place-tapst") ||
                stopId.equals("place-bcnwa") ||
                stopId.equals("place-fbkst") ||
                stopId.equals("place-bndhl") ||
                stopId.equals("place-sumav") ||
                stopId.equals("place-cool") ||
                stopId.equals("place-stpul") ||
                stopId.equals("place-kntst") ||
                stopId.equals("place-hwsst") ||
                stopId.equals("place-smary");
    }

    public static boolean isGreenLineDStop(String stopId) {
        return stopId.equals("place-river") ||
                stopId.equals("place-woodl") ||
                stopId.equals("place-waban") ||
                stopId.equals("place-eliot") ||
                stopId.equals("place-newtn") ||
                stopId.equals("place-newto") ||
                stopId.equals("place-chhil") ||
                stopId.equals("place-rsmnl") ||
                stopId.equals("place-bcnfd") ||
                stopId.equals("place-brkhl") ||
                stopId.equals("place-bvmnl") ||
                stopId.equals("place-longw") ||
                stopId.equals("place-fenwy");
    }

    public static boolean isGreenLineEStop(String stopId) {
        return stopId.equals("place-hsmnl") ||
                stopId.equals("place-bckhl") ||
                stopId.equals("place-rvrwy") ||
                stopId.equals("place-mispk") ||
                stopId.equals("place-fenwd") ||
                stopId.equals("place-brmnl") ||
                stopId.equals("place-lngmd") ||
                stopId.equals("place-mfa") ||
                stopId.equals("place-nuniv") ||
                stopId.equals("place-symcl") ||
                stopId.equals("place-prmnl");
    }
}
