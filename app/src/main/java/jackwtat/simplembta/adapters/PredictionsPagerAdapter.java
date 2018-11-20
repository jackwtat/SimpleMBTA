package jackwtat.simplembta.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import jackwtat.simplembta.fragments.MapSearchFragment;

/**
 * Created by jackw on 8/21/2017.
 */

public class PredictionsPagerAdapter extends FragmentPagerAdapter {
    final private int PAGE_COUNT = 2;
    final private String[] TAB_TITLES = {"Map", "Search"};

    private Fragment[] fragments = new Fragment[2];

    public PredictionsPagerAdapter(FragmentManager fm, Fragment mapSearchFragment,
                                   Fragment manualSearchFragment) {
        super(fm);
        fragments[0] = mapSearchFragment;
        fragments[1] = manualSearchFragment;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
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
