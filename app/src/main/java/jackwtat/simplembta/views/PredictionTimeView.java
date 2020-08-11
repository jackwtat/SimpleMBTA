package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.utilities.Constants;

public class PredictionTimeView extends LinearLayout implements Constants {
    View rootView;
    TextView timeTextView;
    TextView periodTextView;
    TextView statusTextView;

    String min = "";

    public PredictionTimeView(Context context) {
        super(context);
        init(context);
    }

    public PredictionTimeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PredictionTimeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setPrediction(Prediction prediction) {
        clear();

        String[] strings = prediction.toStrings(getContext());

        if (strings.length > 2 &&
                strings[2] != null &&
                !strings[2].equals("")) {
            statusTextView.setText(strings[2]);

        }else {
            timeTextView.setText(strings[0]);

            if (strings.length > 1 && strings[1] != null) {
                periodTextView.setText(strings[1]);
                periodTextView.setVisibility(VISIBLE);
            }

            if (prediction.getStatus() == Prediction.SKIPPED ||
                    prediction.getStatus() == Prediction.CANCELLED) {
                strikeThrough();
            }
        }
    }

    public void strikeThrough() {
        timeTextView.setPaintFlags(timeTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        periodTextView.setPaintFlags(periodTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    public void clear() {
        timeTextView.setText("");
        periodTextView.setText("");
        statusTextView.setText("");

        timeTextView.setPaintFlags(timeTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        periodTextView.setPaintFlags(periodTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.prediction_time_view, this);
        timeTextView = rootView.findViewById(R.id.time_text_view);
        periodTextView = rootView.findViewById(R.id.period_text_view);
        statusTextView = rootView.findViewById(R.id.status_text_view);

        min = context.getResources().getString(R.string.min);
    }
}
