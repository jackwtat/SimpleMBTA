package jackwtat.simplembta.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Stop;

public class StopsSpinnerAdapter extends ArrayAdapter<Stop> {
    private Context context;
    private Stop[] stops;

    public StopsSpinnerAdapter(Context context, Stop[] stops) {
        super(context, 0, stops);
        this.context = context;
        this.stops = stops;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.item_stop_spinner, parent, false);

        Stop stop = stops[position];

        TextView nameTextView = listItem.findViewById(R.id.stop_name_text_view);
        if (stop != null) {
            nameTextView.setText(stop.getName());
        } else {
            nameTextView.setText("null");
        }

        return listItem;
    }

    @Nullable
    @Override
    public Stop getItem(int position) {
        return stops[position];
    }
}
