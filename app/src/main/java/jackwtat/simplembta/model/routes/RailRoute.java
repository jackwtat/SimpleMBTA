package jackwtat.simplembta.model.routes;

import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.model.Stop;

public class RailRoute extends Route {
    public RailRoute(String id) {
        super(id);
    }

    @Override
    public Stop getFocusStop(int directionId) {
        if (super.getFocusStop(directionId) == null)
            setFocusStop(directionId, super.getFocusStop((directionId + 1) % 2));

        return super.getFocusStop(directionId);
    }
}
