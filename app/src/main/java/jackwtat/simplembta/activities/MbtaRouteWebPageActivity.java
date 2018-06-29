package jackwtat.simplembta.activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import jackwtat.simplembta.R;

public class MbtaRouteWebPageActivity extends AppCompatActivity {
    WebView mbtaWebView;
    String routeId;
    String routeName;
    String color;
    int direction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mbta_website_activity);

        routeId = getIntent().getStringExtra("routeId");
        routeName = getIntent().getStringExtra("routeName");
        color = getIntent().getStringExtra("color");
        direction = getIntent().getIntExtra("direction", 0);
        String url = "http://mbta.com/schedules/" + routeId +
                "/line?direction_id=" + direction;

        setTitle(routeName);
        if (Build.VERSION.SDK_INT >= 21) {
            float[] hsv = new float[3];
            Color.colorToHSV(Color.parseColor(color), hsv);
            hsv[2] *= .8f;

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.HSVToColor(hsv));

            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(color)));
        }

        mbtaWebView = findViewById(R.id.mbta_webview);
        mbtaWebView.getSettings().setJavaScriptEnabled(true);
        mbtaWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        mbtaWebView.loadUrl(url);
    }
}
