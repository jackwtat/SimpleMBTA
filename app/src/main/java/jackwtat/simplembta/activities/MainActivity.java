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


public class MainActivity extends AppCompatActivity implements ErrorManager.OnErrorChangedListener {
    public final static String LOG_TAG = "MainActivity";

    private FragmentsPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private ErrorManager errorManager;
    private TextView errorTextView;

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

        errorTextView = findViewById(R.id.error_message_text_view);
    }

    @Override
    protected void onStart() {
        super.onStart();

        errorManager = ErrorManager.getErrorManager();
        errorManager.registerOnErrorChangeListener(this);
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

    @Override
    public void onErrorChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                errorTextView.setOnClickListener(null);

                if (errorManager.hasNetworkError()) {
                    errorTextView.setText(R.string.network_error_text);
                    errorTextView.setVisibility(View.VISIBLE);

                } else if (errorManager.hasLocationPermissionDenied()) {
                    errorTextView.setText(R.string.location_permission_denied_text);
                    errorTextView.setVisibility(View.VISIBLE);
                    errorTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            LocationClient.requestLocationPermission(MainActivity.this);
                        }
                    });

                } else if (errorManager.hasLocationError()) {
                    errorTextView.setText(R.string.location_error_text);
                    errorTextView.setVisibility(View.VISIBLE);

                } else {
                    errorTextView.setVisibility(View.GONE);
                }
            }
        });
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
