package jackwtat.simplembta.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jackwtat.simplembta.R;
import jackwtat.simplembta.adapters.AlertsListAdapter;
import jackwtat.simplembta.mbta.structure.ServiceAlert;

public class AlertsListView extends RelativeLayout {
    View rootView;
    ListView alertsListView;
    LinearLayout noAlertsTextView;
    ImageView listDownArrow;
    ImageView listUpArrow;
    ArrayAdapter<ServiceAlert> alertsArrayAdapter;

    public AlertsListView(Context context) {
        super(context);
    }

    public AlertsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlertsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AlertsListView(Context context, List<ServiceAlert> alerts) {
        super(context);
        init(context, alerts);
    }

    private void init(Context context, List<ServiceAlert> alerts) {
        rootView = inflate(context, R.layout.alerts_list_view, this);
        alertsListView = rootView.findViewById(R.id.alerts_list_view);
        noAlertsTextView = rootView.findViewById(R.id.no_alerts_text_view);
        listDownArrow = rootView.findViewById(R.id.alerts_list_down_arrow);
        listUpArrow = rootView.findViewById(R.id.alerts_list_up_arrow);

        if (alerts.size() > 0) {
            alertsArrayAdapter = new AlertsListAdapter(context, new ArrayList<ServiceAlert>());
            alertsArrayAdapter.addAll(alerts);
            alertsListView.setAdapter(alertsArrayAdapter);

            noAlertsTextView.setVisibility(GONE);

            alertsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {
                    if (alertsListView.getLastVisiblePosition() == alertsListView.getAdapter().getCount() - 1 &&
                            alertsListView.getChildAt(alertsListView.getChildCount() - 1).getBottom() <= alertsListView.getHeight()) {
                        listDownArrow.setVisibility(View.GONE);
                    } else {
                        listDownArrow.setVisibility(View.VISIBLE);
                    }

                    if (alertsListView.getFirstVisiblePosition() == 0) {
                        listUpArrow.setVisibility(View.GONE);
                    } else {
                        listUpArrow.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            noAlertsTextView.setVisibility(VISIBLE);
        }

    }
}
