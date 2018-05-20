package jackwtat.simplembta.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import jackwtat.simplembta.fragments.FavoritesFragment;
import jackwtat.simplembta.fragments.MapFragment;
import jackwtat.simplembta.fragments.NearbyFragment;

/**
 * Created by jackw on 8/21/2017.
 */

public class PredictionsPagerAdapter extends FragmentPagerAdapter {
    final private int PAGE_COUNT = 1;
    final private String[] TAB_TITLES = {"Nearby", "Map", "Favorites"};

    private Fragment[] fragments = new Fragment[3];

    public PredictionsPagerAdapter(FragmentManager fm,
                                   NearbyFragment nearbyFragment,
                                   MapFragment mapFragment,
                                   FavoritesFragment favoritesFragment) {
        super(fm);
        fragments[0] = nearbyFragment;
        fragments[1] = mapFragment;
        fragments[2] = favoritesFragment;
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
