package jackwtat.simplembta.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Stop;

public class StopSpinnerItem extends LinearLayout {
    private View rootView;
    private TextView stopNameTextView;

    private Stop stop;

    public StopSpinnerItem(Context context) {
        super(context);
        init(context);
    }

    public StopSpinnerItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StopSpinnerItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setStop(Stop stop) {
        this.stop = stop;
        stopNameTextView.setText(stop.getName());
    }

    public Stop getStop() {
        return stop;
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.item_stop_spinner, this);
        stopNameTextView = rootView.findViewById(R.id.stop_name_text_view);
    }
}
