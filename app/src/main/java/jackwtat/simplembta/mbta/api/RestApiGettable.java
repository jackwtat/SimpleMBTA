package jackwtat.simplembta.mbta.api;

import java.util.HashMap;

/**
 * Created by jackw on 11/29/2017.
 */

public interface RestApiGettable {
    String get(String query, HashMap<String, String> params);
}
