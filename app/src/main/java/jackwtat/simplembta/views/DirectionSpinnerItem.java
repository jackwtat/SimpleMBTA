package jackwtat.simplembta.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Direction;

public class DirectionSpinnerItem extends LinearLayout {
    private View rootView;
    private TextView directionNameTextView;

    private Direction direction;

    public DirectionSpinnerItem(Context context) {
        super(context);
        init(context);
    }

    public DirectionSpinnerItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DirectionSpinnerItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
        directionNameTextView.setText(direction.getName());
    }

    public Direction getDirection() {
        return direction;
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.item_direction_spinner, this);
        directionNameTextView = rootView.findViewById(R.id.direction_name_text_view);
    }
}
