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
import jackwtat.simplembta.model.Direction;

public class DirectionsSpinnerAdapter extends ArrayAdapter<Direction> {
    private Context context;
    private Direction[] directions;

    public DirectionsSpinnerAdapter(Context context, Direction[] directions) {
        super(context, 0, directions);
        this.context = context;
        this.directions = directions;
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
            listItem = LayoutInflater.from(context).inflate(R.layout.item_direction_spinner, parent, false);

        Direction direction = directions[position];

        TextView nameTextView = listItem.findViewById(R.id.direction_name_text_view);
        nameTextView.setText(direction.getName());

        return listItem;
    }

    @Nullable
    @Override
    public Direction getItem(int position) {
        return directions[position];
    }
}
