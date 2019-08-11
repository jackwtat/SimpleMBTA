package jackwtat.simplembta.map.markers;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import jackwtat.simplembta.R;

public class TransferStopMarkerFactory extends StopMarkerFactory {
    public TransferStopMarkerFactory(){}

    @Override
    public BitmapDescriptor getIcon() {
        return BitmapDescriptorFactory.fromResource(R.drawable.icon_stop_transfer);
    }
}
