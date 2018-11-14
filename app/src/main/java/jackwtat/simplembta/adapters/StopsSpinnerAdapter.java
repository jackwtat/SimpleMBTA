package jackwtat.simplembta.adapters;

import android.content.Context;
import android.graphics.Typeface;
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
    private Stop selectedStop;

    public StopsSpinnerAdapter(Context context, Stop[] stops) {
        super(context, 0, stops);
        this.context = context;
        this.stops = stops;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(stops[position], parent, R.layout.item_stop_selected);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem;
        Stop stop = stops[position];

        if (stop.equals(selectedStop)) {
            listItem = createItemView(stop, parent, R.layout.item_stop_dropdown_selected);
        } else {
            listItem = createItemView(stop, parent, R.layout.item_stop_dropdown);
        }

        return listItem;
    }

    private View createItemView(Stop stop, @NonNull ViewGroup parent, @NonNull int layout) {
        View listItem = LayoutInflater.from(context).inflate(layout, parent, false);

        TextView nameTextView = listItem.findViewById(R.id.stop_name_text_view);

        if (stop != null)
            nameTextView.setText(stop.getName());
        else
            nameTextView.setText("null");

        return listItem;
    }

    @Nullable
    @Override
    public Stop getItem(int position) {
        return stops[position];
    }

    public void setSelectedStop(Stop stop) {
        selectedStop = stop;
    }
}
