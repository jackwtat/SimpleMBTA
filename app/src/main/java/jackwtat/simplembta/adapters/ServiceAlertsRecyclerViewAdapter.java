package jackwtat.simplembta.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.views.ServiceAlertItem;

/**
 * Created by jackw on 3/14/2018.
 */

public class ServiceAlertsRecyclerViewAdapter
        extends RecyclerView.Adapter<ServiceAlertsRecyclerViewAdapter.ViewHolder> {
    final private String LOG_TAG = "ServiceAlertRecyclerViewAdapter";

    ArrayList<ServiceAlert> serviceAlerts = new ArrayList<>();
    private OnItemClickListener onItemClickListener = null;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new ViewHolder(new ServiceAlertItem(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        final int position = i;

        ServiceAlert alert = serviceAlerts.get(position);

        holder.serviceAlertView.setServiceAlert(alert);

        if (position < serviceAlerts.size() - 1) {
            holder.serviceAlertView.enableBorder(true);
        } else {
            holder.serviceAlertView.enableBorder(false);
        }

        if (onItemClickListener != null) {
            holder.serviceAlertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return serviceAlerts.size();
    }

    public ServiceAlert getServiceAlert(int position) {
        if (position < serviceAlerts.size()) {
            return serviceAlerts.get(position);
        } else {
            return null;
        }
    }

    public void setServiceAlerts(List<ServiceAlert> serviceAlerts) {
        this.serviceAlerts.clear();
        this.serviceAlerts.addAll(serviceAlerts);
        Collections.sort(this.serviceAlerts);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int i);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ServiceAlertItem serviceAlertView;

        ViewHolder(ServiceAlertItem v) {
            super(v);
            serviceAlertView = v;
        }
    }
}
