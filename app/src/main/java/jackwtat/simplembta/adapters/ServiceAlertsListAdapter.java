package jackwtat.simplembta.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.ServiceAlert;

/**
 * Created by jackw on 3/14/2018.
 */

public class ServiceAlertsListAdapter extends ArrayAdapter<ServiceAlert> {
    final private String LOG_TAG = "ServiceAlertListAdapter";

    public ServiceAlertsListAdapter(@NonNull Context context, List<ServiceAlert> alerts) {
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
                    R.layout.item_service_alert, parent, false);
        }

        // Check if alert is null
        if (alert == null) {
            return listItemView;
        }

        TextView header = listItemView.findViewById(R.id.alert_header_text_view);
        final TextView shortBody = listItemView.findViewById(R.id.alert_short_body_text_view);
        final TextView longBody = listItemView.findViewById(R.id.alert_long_body_text_view);
        ImageView alertIcon = listItemView.findViewById(R.id.service_alert_icon);
        ImageView advisoryIcon = listItemView.findViewById(R.id.service_advisory_icon);
        ImageView externalLinkIcon = listItemView.findViewById(R.id.external_link_icon);

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

        String alertHeader = headerBuilder.toString();
        String alertShortBody = alert.getHeader();
        String alertLongBody = alert.getDescription();

        header.setText(alertHeader);
        shortBody.setText(alertShortBody);

        if (alertLongBody.length() > 0 && !alertLongBody.equals("null")) {
            longBody.setText(alertLongBody);
            longBody.setVisibility(View.VISIBLE);

            shortBody.setTypeface(null, Typeface.BOLD);
        } else {
            longBody.setVisibility(View.GONE);

            shortBody.setTypeface(null, Typeface.NORMAL);
        }

        if (alert.isActive() && (alert.getLifecycle() == ServiceAlert.Lifecycle.NEW ||
                alert.getLifecycle() == ServiceAlert.Lifecycle.UNKNOWN) ||
                alert.getEffect().equalsIgnoreCase("cancellation") ||
                alert.getEffect().equalsIgnoreCase("delay") ||
                alert.getEffect().equalsIgnoreCase("detour") ||
                alert.getEffect().equalsIgnoreCase("snow route") ||
                alert.getEffect().equalsIgnoreCase("suspension")) {
            alertIcon.setVisibility(View.VISIBLE);
            advisoryIcon.setVisibility(View.GONE);
        } else {
            alertIcon.setVisibility(View.GONE);
            advisoryIcon.setVisibility(View.VISIBLE);
        }

        if (alert.getUrl() != null && !alert.getUrl().equals("") &&
                !alert.getUrl().equalsIgnoreCase("null")) {
            externalLinkIcon.setVisibility(View.VISIBLE);
        } else {
            externalLinkIcon.setVisibility(View.GONE);
        }

        return listItemView;
    }
}
