package jackwtat.simplembta.views;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import jackwtat.simplembta.R;
import jackwtat.simplembta.activities.WebViewActivity;
import jackwtat.simplembta.adapters.ServiceAlertsRecyclerViewAdapter;
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.model.Stop;

public class StopInfoBodyView extends RelativeLayout {
    View rootView;
    View middleBorder;
    View accessibleView;
    View notAccessibleView;
    View noAccessibilityDataView;
    RecyclerView alertsRecyclerView;
    ServiceAlertsRecyclerViewAdapter alertsRecyclerViewAdapter;

    public StopInfoBodyView(Context context) {
        super(context);
        init(context);
    }

    public StopInfoBodyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StopInfoBodyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setAlerts(List<ServiceAlert> alerts) {
        alertsRecyclerViewAdapter.setServiceAlerts(alerts);

        if (alerts.size() > 0) {
            middleBorder.setVisibility(VISIBLE);
        } else {
            middleBorder.setVisibility(GONE);
        }
    }

    public void setAccessibility(Stop.Accessibility accessibility) {
        switch (accessibility) {
            case ACCESSIBLE:
                accessibleView.setVisibility(VISIBLE);
                notAccessibleView.setVisibility(GONE);
                noAccessibilityDataView.setVisibility(GONE);
                break;
            case NOT_ACCESSIBLE:
                accessibleView.setVisibility(GONE);
                notAccessibleView.setVisibility(VISIBLE);
                noAccessibilityDataView.setVisibility(GONE);
                break;
            default:
                accessibleView.setVisibility(GONE);
                notAccessibleView.setVisibility(GONE);
                noAccessibilityDataView.setVisibility(VISIBLE);
        }
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.stop_info_body_view, this);
        middleBorder = rootView.findViewById(R.id.middle_border);
        accessibleView = rootView.findViewById(R.id.accessible_view);
        notAccessibleView = rootView.findViewById(R.id.not_accessible_view);
        noAccessibilityDataView = rootView.findViewById(R.id.no_accessibility_data_view);
        alertsRecyclerView = rootView.findViewById(R.id.alerts_recycler_view);

        alertsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        alertsRecyclerViewAdapter = new ServiceAlertsRecyclerViewAdapter();
        alertsRecyclerViewAdapter.setOnItemClickListener(new ServiceAlertsRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int i) {
                ServiceAlert alert = alertsRecyclerViewAdapter.getServiceAlert(i);

                if (alert != null &&
                        alert.getUrl() != null &&
                        !alert.getUrl().equalsIgnoreCase("") &&
                        !alert.getUrl().equalsIgnoreCase("null")) {
                    Intent intent = new Intent(getContext(), WebViewActivity.class);
                    intent.putExtra("url", alert.getUrl());
                    getContext().startActivity(intent);
                }
            }
        });
        alertsRecyclerView.setAdapter(alertsRecyclerViewAdapter);
    }
}
