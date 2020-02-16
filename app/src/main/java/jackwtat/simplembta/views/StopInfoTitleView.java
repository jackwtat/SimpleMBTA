package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import jackwtat.simplembta.R;

public class StopInfoTitleView extends LinearLayout {
    private View rootView;
    private View[] secondaryColors;
    private TextView textView;

    public StopInfoTitleView(Context context) {
        super(context);
        init(context);
    }

    public StopInfoTitleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StopInfoTitleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void addSecondaryColor(int color) {
        int i = 0;

        while (i < secondaryColors.length && secondaryColors[i].getVisibility() == VISIBLE) {
            i++;
        }

        if (i < secondaryColors.length && secondaryColors[i].getVisibility() != VISIBLE) {
            Drawable background = secondaryColors[i].getBackground();
            DrawableCompat.setTint(background, color);
            secondaryColors[i].setVisibility(VISIBLE);
        }
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

    private void init(Context context){
        rootView = inflate(context, R.layout.stop_info_title_view, this);
        secondaryColors = new View[3];
        secondaryColors[0] = rootView.findViewById(R.id.secondary_color_0);
        secondaryColors[1] = rootView.findViewById(R.id.secondary_color_1);
        secondaryColors[2] = rootView.findViewById(R.id.secondary_color_2);
        textView = rootView.findViewById(R.id.title_text_view);
    }
}
