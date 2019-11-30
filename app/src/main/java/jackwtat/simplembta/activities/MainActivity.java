package jackwtat.simplembta.activities;

import android.location.Location;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;

import jackwtat.simplembta.fragments.RouteSearchFragment;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.R;
import jackwtat.simplembta.adapters.FragmentsPagerAdapter;
import jackwtat.simplembta.fragments.MapSearchFragment;
import jackwtat.simplembta.utilities.PastPredictionsHolder;


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

        PastPredictionsHolder.getHolder().clear();
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

    public interface OutsideQueryListener {
        void outsideQuery(Route route, int directionId, Location location);
    }

    public static void registerOutsideQueryListener(OutsideQueryListener listener) {
        outsideQueryListener = listener;
    }

    public static void deregisterOutsideQueryListener() {
        outsideQueryListener = null;
    }
}
