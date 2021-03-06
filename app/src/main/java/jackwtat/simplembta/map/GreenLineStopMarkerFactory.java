package jackwtat.simplembta.map;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import jackwtat.simplembta.R;

public class GreenLineStopMarkerFactory extends StopMarkerFactory {
    public GreenLineStopMarkerFactory(){}

    @Override
    public BitmapDescriptor getIcon() {
        return BitmapDescriptorFactory.fromResource(R.drawable.icon_stop_green);
    }
}
