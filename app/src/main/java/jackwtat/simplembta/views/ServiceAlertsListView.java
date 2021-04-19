package jackwtat.simplembta.views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import jackwtat.simplembta.R;
import jackwtat.simplembta.activities.WebViewActivity;
import jackwtat.simplembta.adapters.ServiceAlertsRecyclerViewAdapter;
import jackwtat.simplembta.model.ServiceAlert;

public class ServiceAlertsListView extends RelativeLayout {
    View rootView;
    RecyclerView recyclerView;
    ServiceAlertsRecyclerViewAdapter recyclerViewAdapter;

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
        recyclerView = rootView.findViewById(R.id.alerts_recycler_view);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recyclerViewAdapter = new ServiceAlertsRecyclerViewAdapter();
        recyclerViewAdapter.setOnItemClickListener(new ServiceAlertsRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int i) {
                ServiceAlert alert = recyclerViewAdapter.getServiceAlert(i);

                if (alert != null &&
                        alert.getUrl() != null &&
                        !alert.getUrl().equalsIgnoreCase("") &&
                        !alert.getUrl().equalsIgnoreCase("null")) {
                    getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(alert.getUrl())));
                }
            }
        });
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setServiceAlerts(alerts);
    }
}
