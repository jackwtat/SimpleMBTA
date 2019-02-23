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

import java.util.ArrayList;
import java.util.List;

import jackwtat.simplembta.R;
import jackwtat.simplembta.adapters.ServiceAlertsListAdapter;
import jackwtat.simplembta.model.ServiceAlert;

public class ServiceAlertsListView extends RelativeLayout {
    View rootView;
    ListView alertsListView;
    ImageView listDownArrow;
    ImageView listUpArrow;
    LinearLayout noAlertsLayout;
    ArrayAdapter<ServiceAlert> alertsArrayAdapter;

    public ServiceAlertsListView(Context context) {
        super(context);
    }

    public ServiceAlertsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ServiceAlertsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ServiceAlertsListView(Context context, List<ServiceAlert> alerts) {
        super(context);
        init(context, alerts);
    }

    private void init(Context context, List<ServiceAlert> alerts) {
        rootView = inflate(context, R.layout.service_alerts_list_view, this);
        alertsListView = rootView.findViewById(R.id.alerts_list_view);
        listDownArrow = rootView.findViewById(R.id.alerts_list_down_arrow);
        listUpArrow = rootView.findViewById(R.id.alerts_list_up_arrow);
        noAlertsLayout = rootView.findViewById(R.id.no_alerts_layout);

        if (alerts.size() > 0) {
            alertsArrayAdapter = new ServiceAlertsListAdapter(context, alerts);

            alertsListView.setAdapter(alertsArrayAdapter);

            alertsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {
                    if (alertsListView.getLastVisiblePosition() == alertsListView.getAdapter().getCount() - 1 &&
                            alertsListView.getChildAt(alertsListView.getChildCount() - 1).getBottom() <= alertsListView.getHeight())
                        listDownArrow.setVisibility(View.GONE);
                    else
                        listDownArrow.setVisibility(View.VISIBLE);

                    if (alertsListView.getFirstVisiblePosition() == 0)
                        listUpArrow.setVisibility(View.GONE);
                    else
                        listUpArrow.setVisibility(View.VISIBLE);
                }
            });

            alertsListView.setVisibility(VISIBLE);
            listDownArrow.setVisibility(VISIBLE);
            listUpArrow.setVisibility(VISIBLE);
            noAlertsLayout.setVisibility(GONE);
        } else {
            noAlertsLayout.setVisibility(VISIBLE);
            alertsListView.setVisibility(GONE);
            listDownArrow.setVisibility(GONE);
            listUpArrow.setVisibility(GONE);
        }
    }
}
