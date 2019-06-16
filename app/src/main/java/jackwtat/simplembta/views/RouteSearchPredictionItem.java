package jackwtat.simplembta.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.routes.Route;

public class RouteSearchPredictionItem extends LinearLayout {
    View rootView;
    View mainContent;
    ServiceAlertsIndicatorView serviceAlertsIndicatorView;
    TextView noPredictionsTextView;
    TextView timeTextView;
    TextView minuteTextView;
    TextView liveIndicator;
    TextView tomorrowIndicator;
    TextView dropOffIndicator;
    TextView destinationTextView;
    ImageView enrouteIcon;
    View bottomDivider;
    View bottomBorder;
    View onClickAnimation;

    String min;

    public RouteSearchPredictionItem(Context context) {
        super(context);
        init(context);
    }

    public RouteSearchPredictionItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RouteSearchPredictionItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setPrediction(Prediction prediction) {
        String timeText;
        String minuteText;

        long countdownTime = prediction.getCountdownTime();
        Date predictionTime = prediction.getPredictionTime();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(prediction.getPredictionTime());
        int predictionDay = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.setTime(new Date());
        int todayDay = calendar.get(Calendar.DAY_OF_MONTH);

        if (countdownTime <= 60 * 60000) {
            if (countdownTime > 0)
                countdownTime += 15000;
            timeText = (countdownTime / 60000) + "";
            minuteText = min;
        } else {
            timeText = new SimpleDateFormat("h:mm").format(predictionTime);
            minuteText = new SimpleDateFormat("a").format(predictionTime).toLowerCase();
        }

        timeTextView.setText(timeText);
        minuteTextView.setText(minuteText);

        // Show the appropriate status indicators
        if (!prediction.willPickUpPassengers()) {
            dropOffIndicator.setVisibility(VISIBLE);
            liveIndicator.setVisibility(GONE);
        } else if (prediction.isLive()) {
            liveIndicator.setVisibility(VISIBLE);
            dropOffIndicator.setVisibility(GONE);
        }
        if (todayDay - predictionDay < 0) {
            tomorrowIndicator.setVisibility(VISIBLE);
        }
        if (liveIndicator.getVisibility() == GONE &&
                dropOffIndicator.getVisibility() == GONE &&
                tomorrowIndicator.getVisibility() == GONE) {
            liveIndicator.setVisibility(INVISIBLE);
        }

        destinationTextView.setText(prediction.getDestination());

        mainContent.setVisibility(VISIBLE);
        bottomDivider.setVisibility(VISIBLE);
        bottomBorder.setVisibility(GONE);
        serviceAlertsIndicatorView.setVisibility(GONE);
        noPredictionsTextView.setVisibility(GONE);
    }

    public void setServiceAlerts(Route route) {
        serviceAlertsIndicatorView.setServiceAlerts(route);
        serviceAlertsIndicatorView.setVisibility(VISIBLE);

        mainContent.setVisibility(GONE);
        bottomDivider.setVisibility(GONE);
        noPredictionsTextView.setVisibility(GONE);
        bottomBorder.setVisibility(GONE);
    }

    public void setNoPredictionsTextView(String message) {
        noPredictionsTextView.setText(message);
        noPredictionsTextView.setVisibility(VISIBLE);

        mainContent.setVisibility(GONE);
        bottomDivider.setVisibility(GONE);
        serviceAlertsIndicatorView.setVisibility(GONE);
        bottomBorder.setVisibility(GONE);
    }

    public void setBottomBorderVisible() {
        bottomBorder.setVisibility(VISIBLE);

        mainContent.setVisibility(GONE);
        bottomDivider.setVisibility(GONE);
        serviceAlertsIndicatorView.setVisibility(GONE);
        noPredictionsTextView.setVisibility(GONE);
    }

    public void enableOnClickAnimation(boolean enabled) {
        if (enabled)
            onClickAnimation.setVisibility(VISIBLE);
        else
            onClickAnimation.setVisibility(GONE);
    }

    public void clear() {
        serviceAlertsIndicatorView.setVisibility(GONE);
        noPredictionsTextView.setVisibility(GONE);
        noPredictionsTextView.setText("");
        timeTextView.setText("");
        minuteTextView.setText("");
        liveIndicator.setVisibility(GONE);
        tomorrowIndicator.setVisibility(GONE);
        dropOffIndicator.setVisibility(GONE);
        destinationTextView.setText("");
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.item_route_search_prediction, this);
        mainContent = rootView.findViewById(R.id.main_content);
        serviceAlertsIndicatorView = rootView.findViewById(R.id.service_alerts_indicator_view);
        noPredictionsTextView = rootView.findViewById(R.id.no_predictions_text_view);
        timeTextView = rootView.findViewById(R.id.time_text_view);
        minuteTextView = rootView.findViewById(R.id.minute_text_view);
        liveIndicator = rootView.findViewById(R.id.live_text_view);
        tomorrowIndicator = rootView.findViewById(R.id.tomorrow_text_view);
        dropOffIndicator = rootView.findViewById(R.id.drop_off_text_view);
        destinationTextView = rootView.findViewById(R.id.destination_text_view);
        enrouteIcon = rootView.findViewById(R.id.enroute_icon);
        bottomDivider = rootView.findViewById(R.id.bottom_divider);
        bottomBorder = rootView.findViewById(R.id.bottom_border);
        onClickAnimation = rootView.findViewById(R.id.on_click_animation);

        min = context.getResources().getString(R.string.min);
    }
}
