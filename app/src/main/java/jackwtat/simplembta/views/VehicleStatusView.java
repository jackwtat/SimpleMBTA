package jackwtat.simplembta.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Route;

public class VehicleStatusView extends LinearLayout {
    public static final String LOG_TAG = "VehicleStatusView";

    private View rootView;
    private TextView vehicleStatusTextView;
    private ImageView vehicleLightRailIcon;
    private ImageView vehicleHeavyRailIcon;
    private ImageView vehicleCommuterRailIcon;
    private ImageView vehicleBusIcon;
    private ImageView vehicleFerryIcon;

    public VehicleStatusView(Context context) {
        super(context);
        init(context);
    }

    public VehicleStatusView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VehicleStatusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setVehicleStatusText(String status) {
        vehicleStatusTextView.setText(status);
    }

    public void setVehicleIcon(int mode) {
        switch (mode) {
            case Route.LIGHT_RAIL:
                vehicleLightRailIcon.setVisibility(VISIBLE);
                break;
            case Route.HEAVY_RAIL:
                vehicleHeavyRailIcon.setVisibility(VISIBLE);
                break;
            case Route.COMMUTER_RAIL:
                vehicleCommuterRailIcon.setVisibility(VISIBLE);
                break;
            case Route.BUS:
                vehicleBusIcon.setVisibility(VISIBLE);
                break;
            case Route.FERRY:
                vehicleFerryIcon.setVisibility(VISIBLE);
                break;
            default:
                vehicleBusIcon.setVisibility(VISIBLE);
                break;
        }
    }

    public void clear() {
        vehicleStatusTextView.setText("");
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.vehicle_status_view, this);

        vehicleStatusTextView = rootView.findViewById(R.id.vehicle_status_text_view);

        vehicleLightRailIcon = rootView.findViewById(R.id.vehicle_light_rail_icon);
        vehicleHeavyRailIcon = rootView.findViewById(R.id.vehicle_heavy_rail_icon);
        vehicleCommuterRailIcon = rootView.findViewById(R.id.vehicle_commuter_rail_icon);
        vehicleBusIcon = rootView.findViewById(R.id.vehicle_bus_icon);
        vehicleFerryIcon = rootView.findViewById(R.id.vehicle_ferry_icon);
    }
}
