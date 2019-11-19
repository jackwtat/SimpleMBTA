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
    TextView trackNumberIndicator;
    TextView tomorrowIndicator;
    TextView dropOffIndicator;
    TextView destinationTextView;
    TextView vehicleNumberTextView;
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

        long countdownTime = prediction.getCountdownTime() + 15000;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(prediction.getPredictionTime());
        int predictionDay = calendar.get(Calendar.DAY_OF_MONTH);
        int predictionMonth = calendar.get(Calendar.MONTH);
        int predictionYear = calendar.get(Calendar.YEAR);

        calendar.setTime(new Date());
        int todayDay = calendar.get(Calendar.DAY_OF_MONTH);
        int todayMonth = calendar.get(Calendar.MONTH);
        int todayYear = calendar.get(Calendar.YEAR);

        if (countdownTime <= 60 * 60000) {
            if (countdownTime > 0) {
                countdownTime /= 60000;
            }

            if (countdownTime > 0 || !prediction.isLive()) {
                timeText = countdownTime + "";
                minuteText = min;

                if (!prediction.isLive()) {
                    minuteText += "*";
                }

                timeTextView.setText(timeText);
                minuteTextView.setText(minuteText);
                minuteTextView.setVisibility(VISIBLE);
            } else {
                if (prediction.getPredictionType() == Prediction.DEPARTURE) {
                    timeText = getContext().getResources().getString(R.string.departing);
                } else {
                    timeText = getContext().getResources().getString(R.string.arriving);
                }

                timeTextView.setText(timeText);
                minuteTextView.setVisibility(GONE);
            }
        } else {
            Date predictionTime = prediction.getPredictionTime();
            timeText = new SimpleDateFormat("h:mm").format(predictionTime);
            minuteText = new SimpleDateFormat("a").format(predictionTime).toLowerCase();

            if (!prediction.isLive()) {
                minuteText += "*";
            }

            timeTextView.setText(timeText);
            minuteTextView.setText(minuteText);
            minuteTextView.setVisibility(VISIBLE);
        }

        // Show the appropriate status indicators
        if (!prediction.willPickUpPassengers()) {
            dropOffIndicator.setVisibility(VISIBLE);
            liveIndicator.setVisibility(GONE);
        } else if (prediction.isLive()) {
            int mode = prediction.getRoute().getMode();
            String trackNumber = prediction.getTrackNumber();

            if (mode == Route.COMMUTER_RAIL &&
                    trackNumber != null && !trackNumber.equals("") && !trackNumber.equals("null")) {
                trackNumber = getContext().getResources().getString(R.string.track) + " " + trackNumber;
                trackNumberIndicator.setText(trackNumber);
                trackNumberIndicator.setVisibility(VISIBLE);
                liveIndicator.setVisibility(GONE);

            } else {
                trackNumberIndicator.setVisibility(GONE);
                liveIndicator.setVisibility(VISIBLE);
            }
        }
        if (predictionDay - todayDay > 0 ||
                predictionMonth - todayMonth > 0 ||
                predictionYear - todayYear > 0) {
            tomorrowIndicator.setVisibility(VISIBLE);
        }
        if (liveIndicator.getVisibility() == GONE &&
                trackNumberIndicator.getVisibility() == GONE &&
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

    public void setTrainNumber(String trainNumber) {
        String text = getResources().getString(R.string.train) +
                " " + trainNumber;

        vehicleNumberTextView.setText(text);
        vehicleNumberTextView.setVisibility(VISIBLE);
    }

    public void setVehicleNumber(String vehicleNumber) {
        if (vehicleNumber.substring(0, 1).equals("y")) {
            vehicleNumber = vehicleNumber.substring(1);
        }

        String text = getResources().getString(R.string.vehicle) +
                " " + vehicleNumber;

        vehicleNumberTextView.setText(text);
        vehicleNumberTextView.setVisibility(VISIBLE);
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
        trackNumberIndicator.setVisibility(GONE);
        tomorrowIndicator.setVisibility(GONE);
        dropOffIndicator.setVisibility(GONE);
        destinationTextView.setText("");
        vehicleNumberTextView.setText("");
        vehicleNumberTextView.setVisibility(GONE);
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.item_route_search_prediction, this);
        mainContent = rootView.findViewById(R.id.main_content);
        serviceAlertsIndicatorView = rootView.findViewById(R.id.service_alerts_indicator_view);
        noPredictionsTextView = rootView.findViewById(R.id.no_predictions_text_view);
        timeTextView = rootView.findViewById(R.id.time_text_view);
        minuteTextView = rootView.findViewById(R.id.minute_text_view);
        liveIndicator = rootView.findViewById(R.id.live_text_view);
        trackNumberIndicator = rootView.findViewById(R.id.track_number_text_view);
        tomorrowIndicator = rootView.findViewById(R.id.tomorrow_text_view);
        dropOffIndicator = rootView.findViewById(R.id.drop_off_text_view);
        destinationTextView = rootView.findViewById(R.id.destination_text_view);
        vehicleNumberTextView = rootView.findViewById(R.id.vehicle_number_text_view);
        enrouteIcon = rootView.findViewById(R.id.enroute_icon);
        bottomDivider = rootView.findViewById(R.id.bottom_divider);
        bottomBorder = rootView.findViewById(R.id.bottom_border);
        onClickAnimation = rootView.findViewById(R.id.on_click_animation);

        min = context.getResources().getString(R.string.min);
    }
}
