package jackwtat.simplembta.activities;

import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import jackwtat.simplembta.fragments.RouteSearchFragment;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.R;
import jackwtat.simplembta.adapters.FragmentsPagerAdapter;
import jackwtat.simplembta.fragments.MapSearchFragment;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.utilities.ErrorManager;
import jackwtat.simplembta.utilities.PastDataHolder;


public class MainActivity extends AppCompatActivity implements MapSearchFragment.PredictionClickListener {
    public final static String LOG_TAG = "MainActivity";

    private static OutsideQueryListener outsideQueryListener;

    private FragmentsPagerAdapter pagerAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MapSearchFragment mapSearchFragment = new MapSearchFragment();

        RouteSearchFragment routeSearchFragment = new RouteSearchFragment();

        Fragment[] fragments = {mapSearchFragment, routeSearchFragment};

        String[] tabTitles = {getResources().getString(R.string.map_search_title),
                getResources().getString(R.string.route_search_title),};

        pagerAdapter = new FragmentsPagerAdapter(getSupportFragmentManager(),
                fragments, tabTitles);

        // Set up the ViewPager with the sections adapter.
        viewPager = findViewById(R.id.fragment_container);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(2);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_map_search);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_route_search);
        tabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.TabSelectedText);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.TabText);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                }
        );
        tabLayout.getTabAt(0).getIcon().setColorFilter(
                ContextCompat.getColor(getApplicationContext(), R.color.TabSelectedText),
                PorterDuff.Mode.SRC_IN);
    }

    @Override
    protected void onResume() {
        super.onResume();

        MapSearchFragment.registerPredictionClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        MapSearchFragment.deregisterPredictionClickListener();
    }

    @Override
    protected void onStop() {
        super.onStop();

        PastDataHolder.getHolder().clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(0);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(Route route, int directionId, Location location) {
        if (outsideQueryListener != null) {
            outsideQueryListener.outsideQuery(route, directionId, location);
        }

        viewPager.setCurrentItem(1);
    }

    @Override
    public void onClick(Route route, int directionId, Stop stop) {
        if (outsideQueryListener != null) {
            outsideQueryListener.outsideQuery(route, directionId, stop);
        }

        viewPager.setCurrentItem(1);
    }

    public interface OutsideQueryListener {
        void outsideQuery(Route route, int directionId, Location location);

        void outsideQuery(Route route, int directionId, Stop stop);
    }

    public static void registerOutsideQueryListener(OutsideQueryListener listener) {
        outsideQueryListener = listener;
    }

    public static void deregisterOutsideQueryListener() {
        outsideQueryListener = null;
    }
}
