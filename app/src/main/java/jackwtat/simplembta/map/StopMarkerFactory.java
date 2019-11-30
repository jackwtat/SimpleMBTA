package jackwtat.simplembta.map;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;

import jackwtat.simplembta.R;

public class StopMarkerFactory implements Serializable {
    public StopMarkerFactory() {
    }

    public MarkerOptions createMarkerOptions() {
        return new MarkerOptions()
                .anchor(0.5f, 0.5f)
                .flat(true)
                .icon(getIcon());
    }

    public BitmapDescriptor getIcon(){
        return BitmapDescriptorFactory.fromResource(R.drawable.icon_stop);
    }
}
