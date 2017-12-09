package jackwtat.simplembta.mbta.api;

import java.util.HashMap;

/**
 * Created by jackw on 11/29/2017.
 */

public abstract class RestApiGetQuery {
    private final RestApiGettable api;
    private String query;

    protected RestApiGetQuery(RestApiGettable api, String query) {
        this.api = api;
        this.query = query;
    }

    public String get(HashMap<String, String> params) {
        return api.get(query, params);
    }
}
