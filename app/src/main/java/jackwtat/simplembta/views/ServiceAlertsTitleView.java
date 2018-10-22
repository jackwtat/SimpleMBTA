package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jackwtat.simplembta.R;

public class ServiceAlertsTitleView extends RelativeLayout {
    View rootView;
    TextView textView;
    View accentView;

    public ServiceAlertsTitleView(Context context) {
        super(context);
        initializeViews(context);
    }

    public ServiceAlertsTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public ServiceAlertsTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews(context);
    }

    public ServiceAlertsTitleView(Context context, String text, int textColor, int backgroundColor,
                                  boolean accentViewVisible) {
        super(context);
        initializeViews(context);
        setText(text);
        setTextColor(textColor);
        setBackgroundColor(backgroundColor);
        setAccentViewVisible(accentViewVisible);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public void setTextColor(int color) {
        textView.setTextColor(color);
    }

    public void setBackgroundColor(int color) {
        Drawable background = textView.getBackground();
        DrawableCompat.setTint(textView.getBackground(), color);
        textView.setBackground(background);
    }

    public void setAccentViewVisible(boolean visible) {
        if (visible) {
            accentView.setVisibility(VISIBLE);
        } else {
            accentView.setVisibility(GONE);
        }
    }

    private void initializeViews(Context context) {
        rootView = inflate(context, R.layout.service_alerts_title_view, this);
        textView = rootView.findViewById(R.id.title_text_view);
        accentView = rootView.findViewById(R.id.title_accent);
    }
}
