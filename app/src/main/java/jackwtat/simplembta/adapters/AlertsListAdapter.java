package jackwtat.simplembta.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import jackwtat.simplembta.R;
import jackwtat.simplembta.mbta.structure.ServiceAlert;

/**
 * Created by jackw on 3/14/2018.
 */

public class AlertsListAdapter extends ArrayAdapter<ServiceAlert> {
    final private String LOG_TAG = "ServiceAlertListAdapter";

    public AlertsListAdapter(
            @NonNull Context context, ArrayList<ServiceAlert> alerts) {
        super(context, 0, alerts);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;

        ServiceAlert alert = getItem(position);

        // Inflate the listItemView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.alert_list_item, parent, false);
        }

        // Check if there are no Predictions
        if (alert == null) {
            return listItemView;
        }

        TextView header = listItemView.findViewById(R.id.alert_header_text_view);
        TextView body = listItemView.findViewById(R.id.alert_body_text_view);

        String headerText = getContext().getResources().getString(
                getContext().getResources().getIdentifier(
                        alert.getEffect(), "string", getContext().getPackageName()));

        StringBuilder headerBuilder;
        if (alert.isActive()) {
            headerBuilder = new StringBuilder(headerText);
        } else {
            headerBuilder = new StringBuilder(getContext().getResources().getString(R.string.UPCOMING))
                    .append(" ").append(headerText);
        }

        header.setText(headerBuilder.toString());
        body.setText(alert.getHeader());

        return listItemView;
    }
}
