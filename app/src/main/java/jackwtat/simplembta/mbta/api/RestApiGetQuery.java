package jackwtat.simplembta.mbta.api;

import java.util.HashMap;

/**
 * Created by jackw on 11/29/2017.
 */

public abstract class RestApiGetQuery implements RestApiGettable {
    protected final RestApiGettable api;

    protected RestApiGetQuery(RestApiGettable api){
        this.api = api;
    }

    @Override
    public String get(String query, HashMap<String, String> params){
        return api.get(query, params);
    }
}
