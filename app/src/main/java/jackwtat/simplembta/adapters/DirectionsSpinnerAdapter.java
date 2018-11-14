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
    private Direction selectedDirection;

    public DirectionsSpinnerAdapter(Context context, Direction[] directions) {
        super(context, 0, directions);
        this.context = context;
        this.directions = directions;

        // If the southbound/outbound direction is first, then swap it with northbound/inbound
        if (directions[0].getId() == Direction.SOUTHBOUND) {
            Direction placeholder = directions[0];
            directions[0] = directions[1];
            directions[1] = placeholder;
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(directions[position], parent, R.layout.item_direction_selected);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem;
        Direction direction = directions[position];

        if (direction.equals(selectedDirection)) {
            listItem = createItemView(direction, parent, R.layout.item_direction_dropdown_selected);
        } else {
            listItem = createItemView(direction, parent, R.layout.item_direction_dropdown);
        }

        return listItem;
    }

    private View createItemView(Direction direction, @NonNull ViewGroup parent, @NonNull int layout) {
        View listItem = LayoutInflater.from(context).inflate(layout, parent, false);

        TextView nameTextView = listItem.findViewById(R.id.direction_name_text_view);
        nameTextView.setText(direction.getName());

        return listItem;
    }

    @Nullable
    @Override
    public Direction getItem(int position) {
        return directions[position];
    }

    public void setSelectedDirection(Direction direction) {
        selectedDirection = direction;
    }
}
