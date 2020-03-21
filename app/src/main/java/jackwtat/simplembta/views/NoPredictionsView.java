package jackwtat.simplembta.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import jackwtat.simplembta.R;

public class NoPredictionsView extends LinearLayout {
    private View rootView;
    private TextView messageTextView;

    boolean error = false;

    public NoPredictionsView(Context context) {
        super(context);
        init(context);
    }

    public NoPredictionsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NoPredictionsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setNoPredictions(String message) {
        if (!error) {
            messageTextView.setText(message);
            messageTextView.setVisibility(VISIBLE);
        }
    }

    public void setError(String message) {
        error = true;
        messageTextView.setText(message);
        messageTextView.setVisibility(VISIBLE);
    }

    public void clearNoPredictions() {
        if (!error) {
            messageTextView.setText("");
            messageTextView.setVisibility(GONE);
        }
    }

    public void clearError() {
        error = false;
        messageTextView.setText("");
        messageTextView.setVisibility(GONE);
    }

    public boolean isError(){
        return error;
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.no_predictions_view, this);
        messageTextView = rootView.findViewById(R.id.message_text_view);
        messageTextView.setVisibility(GONE);
    }
}
