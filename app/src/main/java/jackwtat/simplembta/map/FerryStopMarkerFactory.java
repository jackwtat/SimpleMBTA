package jackwtat.simplembta.map;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import jackwtat.simplembta.R;

public class FerryStopMarkerFactory extends StopMarkerFactory {
    public FerryStopMarkerFactory() {
    }

    @Override
    public BitmapDescriptor getIcon() {
        return BitmapDescriptorFactory.fromResource(R.drawable.icon_stop_ferry);
    }
}
