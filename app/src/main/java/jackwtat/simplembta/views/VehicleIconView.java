package jackwtat.simplembta.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import jackwtat.simplembta.R;

public class VehicleIconView extends RelativeLayout {
    View rootView;

    public VehicleIconView(Context context) {
        super(context);
        init(context);
    }

    public VehicleIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VehicleIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        rootView = inflate(context, R.layout.vehicle_icon_view, this);
    }
}
