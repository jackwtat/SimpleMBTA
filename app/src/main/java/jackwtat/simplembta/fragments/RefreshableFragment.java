package jackwtat.simplembta.fragments;

import android.support.v4.app.Fragment;

public abstract class RefreshableFragment extends Fragment {
    public abstract void refresh();

    public abstract void autoRefresh();

    public abstract void forceRefresh();
}
