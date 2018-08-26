package jackwtat.simplembta.fragments;

import android.support.v4.app.Fragment;

public interface Refreshable {
    void refresh();

    void autoRefresh();

    void forceRefresh();
}
