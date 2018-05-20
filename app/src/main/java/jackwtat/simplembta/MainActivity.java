package jackwtat.simplembta;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import jackwtat.simplembta.adapters.PredictionsPagerAdapter;
import jackwtat.simplembta.fragments.FavoritesFragment;
import jackwtat.simplembta.fragments.MapFragment;
import jackwtat.simplembta.fragments.NearbyFragment;
import jackwtat.simplembta.fragments.RefreshableFragment;

public class MainActivity extends AppCompatActivity {

    private PredictionsPagerAdapter predictionsPagerAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        predictionsPagerAdapter = new PredictionsPagerAdapter(getSupportFragmentManager(),
                new NearbyFragment(), new MapFragment(), new FavoritesFragment());

        // Set up the ViewPager with the sections adapter.
        viewPager = findViewById(R.id.fragment_container);
        viewPager.setAdapter(predictionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i("Main Activity","This is fragment "+predictionsPagerAdapter.getItemPosition(getCurrentRefreshableFragment()));

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_refresh:
                getCurrentRefreshableFragment().refresh();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private RefreshableFragment getCurrentRefreshableFragment() {
        return (RefreshableFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
    }
}
