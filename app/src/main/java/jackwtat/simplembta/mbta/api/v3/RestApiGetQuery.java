package jackwtat.simplembta.mbta.api.v3;

import java.util.HashMap;
import jackwtat.simplembta.mbta.api.v3.V3RealTimeApi;

/**
 * Created by jackw on 11/29/2017.
 */

public abstract class RestApiGetQuery {
    protected final V3RealTimeApi api;
    protected String query;

    protected RestApiGetQuery(V3RealTimeApi api, String query) {
        this.api = api;
        this.query = query;
    }

    protected String get(HashMap<String, String> params) {
        return api.get(query, params);
    }
}
