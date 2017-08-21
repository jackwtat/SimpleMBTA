package jackwtat.simplembta.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by jackw on 8/21/2017.
 */

public class PredictionsPagerAdapter extends FragmentPagerAdapter {
    final private int PAGE_COUNT = 3;
    final private String[] TAB_TITLES = {"Nearby", "Map", "Saved"};

    public PredictionsPagerAdapter(FragmentManager fm){super(fm);}

    @Override
    public Fragment getItem(int position) {
        //TODO: Get Fragments
        return null;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TAB_TITLES[position];
    }
}
