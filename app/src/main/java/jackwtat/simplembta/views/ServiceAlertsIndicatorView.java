package jackwtat.simplembta.views;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.routes.Bus;
import jackwtat.simplembta.model.routes.Route;
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.model.routes.SilverLine;

public class ServiceAlertsIndicatorView extends LinearLayout {
    private View rootView;
    private ImageView serviceAlertIcon;
    private ImageView serviceAdvisoryIcon;
    private TextView serviceAlertsTextView;

    private Route route;
    private int alertsCount = 0;
    private int advisoriesCount = 0;

    public ServiceAlertsIndicatorView(Context context) {
        super(context);
        init(context);
    }

    public ServiceAlertsIndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ServiceAlertsIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ServiceAlertsIndicatorView(Context context, Route route) {
        super(context);
        init(context);
        setServiceAlerts(route);
    }

    public void setServiceAlerts(Route route) {
        this.route = route;

        countAlerts();

        setIcon();
        setText();
        setOnClickDialog();
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.service_alerts_indicator_view, this);
        serviceAlertIcon = rootView.findViewById(R.id.service_alert_icon);
        serviceAdvisoryIcon = rootView.findViewById(R.id.service_advisory_icon);
        serviceAlertsTextView = rootView.findViewById(R.id.service_alerts_text_view);
    }

    private void countAlerts() {
        alertsCount = 0;
        advisoriesCount = 0;

        for (ServiceAlert alert : route.getServiceAlerts()) {
            if (alert.isActive() && (alert.getLifecycle() == ServiceAlert.Lifecycle.NEW ||
                    alert.getLifecycle() == ServiceAlert.Lifecycle.UNKNOWN)) {
                alertsCount++;
            } else {
                advisoriesCount++;
            }
        }
    }

    private void setIcon() {
        if (alertsCount > 0) {
            serviceAlertIcon.setVisibility(VISIBLE);
            serviceAdvisoryIcon.setVisibility(GONE);

        } else if (advisoriesCount > 0) {
            serviceAlertIcon.setVisibility(GONE);
            serviceAdvisoryIcon.setVisibility(VISIBLE);

        } else {
            serviceAlertIcon.setVisibility(GONE);
            serviceAdvisoryIcon.setVisibility(GONE);
        }
    }

    private void setText() {
        String alertsText = "";

        // Show the number of alerts
        if (alertsCount > 0) {
            alertsText = (alertsCount > 1)
                    ? alertsCount + " " + getResources().getString(R.string.alerts)
                    : alertsCount + " " + getResources().getString(R.string.alert);
        }

        // Show the number of advisories
        if (advisoriesCount > 0) {
            alertsText = (alertsCount > 0) ? alertsText + ", " : alertsText;

            alertsText = (advisoriesCount > 1)
                    ? alertsText + advisoriesCount + " " + getResources().getString(R.string.advisories)
                    : alertsText + advisoriesCount + " " + getResources().getString(R.string.advisory);
        }


        // Set the service alerts view
        serviceAlertsTextView.setText(alertsText);
    }

    private void setOnClickDialog() {
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<ServiceAlert> serviceAlerts = route.getServiceAlerts();
                Collections.sort(serviceAlerts);

                AlertDialog dialog = new AlertDialog.Builder(view.getContext()).create();

                dialog.setCustomTitle(new ServiceAlertsTitleView(view.getContext(),
                        (alertsCount > 0)
                                ? (alertsCount + advisoriesCount > 1)
                                    ? view.getContext().getString(R.string.service_alerts)
                                    : view.getContext().getString(R.string.service_alert)
                                : (advisoriesCount > 1)
                                    ? view.getContext().getString(R.string.service_advisories)
                                    : view.getContext().getString(R.string.service_advisory),
                        Color.parseColor(route.getTextColor()),
                        Color.parseColor(route.getPrimaryColor()),
                        route.getMode() == Route.BUS &&
                                !SilverLine.isSilverLine(route.getId())));

                dialog.setView(new ServiceAlertsListView(view.getContext(), serviceAlerts));

                dialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.dialog_close_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                dialog.show();
            }
        });
    }
}