package jackwtat.simplembta.activities;

import android.content.pm.PackageManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import jackwtat.simplembta.ErrorMessageHandler;
import jackwtat.simplembta.R;
import jackwtat.simplembta.adapters.PredictionsPagerAdapter;
import jackwtat.simplembta.fragments.MapSearchFragment;
import jackwtat.simplembta.fragments.NearbyPredictionsFragment;
import jackwtat.simplembta.fragments.RefreshableFragment;


public class MainActivity extends AppCompatActivity implements ErrorMessageHandler.OnErrorChangedListener {

    private final int REQUEST_ACCESS_FINE_LOCATION = 1;

    private PredictionsPagerAdapter predictionsPagerAdapter;
    private ViewPager viewPager;
    private ErrorMessageHandler errorMessageHandler;
    private TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        predictionsPagerAdapter = new PredictionsPagerAdapter(getSupportFragmentManager(),
                new NearbyPredictionsFragment(), new MapSearchFragment());

        // Set up the ViewPager with the sections adapter.
        viewPager = findViewById(R.id.fragment_container);
        viewPager.setAdapter(predictionsPagerAdapter);
        viewPager.setPageMargin((int) (getResources().getDimension(R.dimen.fragment_pager_spacing) *
                getResources().getDisplayMetrics().density));

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        errorMessageHandler = ErrorMessageHandler.getErrorMessageHandler();
        errorMessageHandler.registerOnErrorChangeListener(this);
        errorTextView = findViewById(R.id.error_message_text_view);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Get location access permission from user
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_refresh:
                RefreshableFragment f = (RefreshableFragment) predictionsPagerAdapter.getItem(
                        viewPager.getCurrentItem());
                try {
                    f.forceRefresh();
                } catch (Exception e) {

                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onErrorChanged() {
        if (errorMessageHandler.hasNetworkError()) {
            errorTextView.setText(R.string.network_error_text);
            errorTextView.setVisibility(View.VISIBLE);

        } else if (errorMessageHandler.hasLocationPermissionDenied()) {
            errorTextView.setText(R.string.location_permission_denied_text);
            errorTextView.setVisibility(View.VISIBLE);
            errorTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestLocationPermission();
                }
            });

        } else if (errorMessageHandler.hasLocationError()) {
            errorTextView.setText(R.string.location_error_text);
            errorTextView.setVisibility(View.VISIBLE);

        } else {
            errorTextView.setVisibility(View.GONE);
            errorTextView.setOnClickListener(null);
        }
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_ACCESS_FINE_LOCATION);
    }
}
