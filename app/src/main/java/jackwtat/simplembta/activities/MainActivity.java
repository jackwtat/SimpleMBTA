package jackwtat.simplembta.activities;

import android.location.Location;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import jackwtat.simplembta.clients.LocationClient;
import jackwtat.simplembta.fragments.RouteSearchFragment;
import jackwtat.simplembta.model.Stop;
import jackwtat.simplembta.model.routes.Route;
import jackwtat.simplembta.utilities.ErrorManager;
import jackwtat.simplembta.R;
import jackwtat.simplembta.adapters.FragmentsPagerAdapter;
import jackwtat.simplembta.fragments.MapSearchFragment;


public class MainActivity extends AppCompatActivity {
    public final static String LOG_TAG = "MainActivity";

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

        ((MapSearchFragment) pagerAdapter.getItem(0)).setMainActivity(this);
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

    public void goToRoute(Route route, int directionId, Stop stop) {
        RouteSearchFragment fragment = (RouteSearchFragment) pagerAdapter.getItem(1);
        fragment.query(route, directionId, stop);
        viewPager.setCurrentItem(1);
    }

    public void goToRoute(Route route, int directionId, Location location) {
        RouteSearchFragment fragment = (RouteSearchFragment) pagerAdapter.getItem(1);
        fragment.query(route, directionId, location);
        viewPager.setCurrentItem(1);
    }
}
