package jackwtat.simplembta.model.routes;

import jackwtat.simplembta.map.markers.GreenLineStopMarkerFactory;
import jackwtat.simplembta.model.Direction;
import jackwtat.simplembta.model.Prediction;

public class GreenLine extends Route {


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
        if (prediction.getDirection() == Direction.EASTBOUND ||
                isGreenLineSubwayStop(prediction.getStopId()) || isGreenLineSubwayStop(prediction.getParentStopId()) ||
                (isGreenLineB(prediction.getRouteId()) && isGreenLineBStop(prediction.getStopId())) ||
                (isGreenLineC(prediction.getRouteId()) && isGreenLineCStop(prediction.getStopId())) ||
                (isGreenLineD(prediction.getRouteId()) && isGreenLineDStop(prediction.getStopId())) ||
                (isGreenLineE(prediction.getRouteId()) && isGreenLineEStop(prediction.getStopId())) ||
                (isGreenLineB(prediction.getRouteId()) && isGreenLineBStop(prediction.getParentStopId())) ||
                (isGreenLineC(prediction.getRouteId()) && isGreenLineCStop(prediction.getParentStopId())) ||
                (isGreenLineD(prediction.getRouteId()) && isGreenLineDStop(prediction.getParentStopId())) ||
                (isGreenLineE(prediction.getRouteId()) && isGreenLineEStop(prediction.getParentStopId()))) {
            super.addPrediction(prediction);
        }
    }

    public static boolean isGreenLine(String id) {
        return id.contains("Green-B") ||
                id.contains("Green-C") ||
                id.contains("Green-D") ||
                id.contains("Green-E");
    }

    public static boolean isGreenLineB(String id) {
        return id.equals("Green-B");
    }

    public static boolean isGreenLineC(String id) {
        return id.equals("Green-C");
    }

    public static boolean isGreenLineD(String id) {
        return id.equals("Green-D");
    }

    public static boolean isGreenLineE(String id) {
        return id.equals("Green-E");
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
