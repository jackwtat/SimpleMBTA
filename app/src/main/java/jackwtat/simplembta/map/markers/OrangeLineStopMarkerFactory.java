package jackwtat.simplembta.map.markers;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import jackwtat.simplembta.R;

public class OrangeLineStopMarkerFactory extends StopMarkerFactory {
    public OrangeLineStopMarkerFactory() {

    }

    @Override
    public BitmapDescriptor getIcon() {
        return BitmapDescriptorFactory.fromResource(R.drawable.icon_stop_orange);
    }
}
