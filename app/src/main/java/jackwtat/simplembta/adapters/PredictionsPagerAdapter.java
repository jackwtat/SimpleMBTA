package jackwtat.simplembta.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import jackwtat.simplembta.fragments.NearbyPredictionsFragment;

/**
 * Created by jackw on 8/21/2017.
 */

public class PredictionsPagerAdapter extends FragmentPagerAdapter {
    final private int PAGE_COUNT = 1;
    final private String[] TAB_TITLES = {"Nearby", "Map", "Saved"};

    public PredictionsPagerAdapter(FragmentManager fm){super(fm);}

    @Override
    public Fragment getItem(int position) {
        return new NearbyPredictionsFragment();
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
