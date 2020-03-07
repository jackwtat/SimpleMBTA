package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.ServiceAlert;
import jackwtat.simplembta.model.Vehicle;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.utilities.Constants;

public class TripDetailPredictionItem extends RelativeLayout implements Constants {

    public static final int FIRST_STOP = 0;
    public static final int INTERMEDIATE_STOP = 1;
    public static final int LAST_STOP = 2;
    public static final int ONLY_STOP = 3;

    View rootView;
    View topLineView;
    View bottomLineView;
    LinearLayout predictionInfoLayout;
    LinearLayout vehicleIcons;
    ImageView stopIcon;
    ImageView stopIconFill;
    ImageView stopIconFillCurrent;
    ImageView stopIconCancelled;
    ImageView wheelchairAccessibleIcon;
    ImageView stopAdvisoryIcon;
    ImageView stopAlertIcon;
    TextView stopName;
    TextView timeTextView;
    TextView minuteTextView;
    TextView statusTextView;
    TextView trackNumberTextView;
    TextView cancelledIndicator;
    TextView dropOffIndicator;
    View bottomDivider;
    View bottomEdge;
    View bottomBorder;

    String min;

    public TripDetailPredictionItem(Context context) {
        super(context);
        init(context);
    }

    public TripDetailPredictionItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TripDetailPredictionItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setPrediction(
            Prediction prediction, @Nullable Prediction nextPrediction, int stopSequenceType,
            Vehicle vehicle) {
        // Departure time
        long countdownTime = prediction.getCountdownTime();

        // There is a vehicle currently on this trip and trip is not skipped or cancelled
        if (vehicle != null && vehicle.getTripId().equalsIgnoreCase(prediction.getTripId())) {

            // Vehicle has already passed this stop
            if (vehicle.getCurrentStopSequence() > prediction.getStopSequence() ||
                    (nextPrediction != null && nextPrediction.isLive() &&
                            prediction.getCountdownTime() > nextPrediction.getCountdownTime())) {
                String statusText;
                if (prediction.getPredictionType() == Prediction.DEPARTURE) {
                    statusText = getContext().getResources().getString(R.string.trip_already_departed);
                } else {
                    statusText = getContext().getResources().getString(R.string.trip_already_arrived);
                }

                statusTextView.setText(statusText);

                timeTextView.setVisibility(GONE);
                minuteTextView.setVisibility(GONE);
                statusTextView.setVisibility(VISIBLE);

                // Vehicle is at or approaching this stop
            } else if (vehicle.getCurrentStopSequence() == prediction.getStopSequence()) {

                // Vehicle is more than one minute away
                if (countdownTime > COUNTDOWN_APPROACHING_CUTOFF) {
                    String timeText;
                    String minuteText;

                    if (countdownTime < COUNTDOWN_HOUR_CUTOFF) {
                        timeText = (countdownTime / 60000) + "";
                        minuteText = min;

                    } else {
                        Date predictionTime = prediction.getPredictionTime();
                        timeText = new SimpleDateFormat("h:mm").format(predictionTime);
                        minuteText = new SimpleDateFormat("a").format(predictionTime).toLowerCase();
                    }

                    timeTextView.setText(timeText);
                    minuteTextView.setText(minuteText);

                    timeTextView.setVisibility(VISIBLE);
                    minuteTextView.setVisibility(VISIBLE);
                    statusTextView.setVisibility(GONE);

                    // Vehicle is less than one minute away
                } else {
                    String statusText;

                    if (prediction.getPredictionType() == Prediction.DEPARTURE &&
                            vehicle.getCurrentStatus() == Vehicle.Status.STOPPED) {
                        statusText = getContext().getResources().getString(R.string.trip_departing);

                    } else if (countdownTime < COUNTDOWN_ARRIVING_CUTOFF) {
                        statusText = getContext().getResources().getString(R.string.trip_arriving);

                    } else {
                        statusText = getContext().getResources().getString(R.string.trip_approaching);
                    }

                    statusTextView.setText(statusText);

                    timeTextView.setVisibility(GONE);
                    minuteTextView.setVisibility(GONE);
                    statusTextView.setVisibility(VISIBLE);
                }

                // Vehicle is not yet approaching this stop
            } else {
                String timeText;
                String minuteText;

                if (countdownTime < COUNTDOWN_HOUR_CUTOFF) {
                    if (countdownTime > 0) {
                        timeText = (countdownTime / 60000) + "";
                    } else {
                        timeText = "0";
                    }
                    minuteText = min;

                } else {
                    Date predictionTime = prediction.getPredictionTime();
                    timeText = new SimpleDateFormat("h:mm").format(predictionTime);
                    minuteText = new SimpleDateFormat("a").format(predictionTime).toLowerCase();
                }

                timeTextView.setText(timeText);
                minuteTextView.setText(minuteText);

                timeTextView.setVisibility(VISIBLE);
                minuteTextView.setVisibility(VISIBLE);
                statusTextView.setVisibility(GONE);
            }

            // No vehicle is on this trip or trip is skipped or cancelled
        } else {
            if (countdownTime > 0) {
                String timeText;
                String minuteText;

                if (countdownTime < 3600000) {
                    timeText = (countdownTime / 60000) + "";
                    minuteText = min;

                } else {
                    Date predictionTime = prediction.getPredictionTime();
                    timeText = new SimpleDateFormat("h:mm").format(predictionTime);
                    minuteText = new SimpleDateFormat("a").format(predictionTime).toLowerCase();
                }

                if (!prediction.isLive()) {
                    minuteText += "*";
                }

                timeTextView.setText(timeText);
                minuteTextView.setText(minuteText);

                timeTextView.setVisibility(VISIBLE);
                minuteTextView.setVisibility(VISIBLE);
                statusTextView.setVisibility(GONE);
            } else {
                String statusText;

                if (prediction.getPredictionType() == Prediction.DEPARTURE) {
                    statusText = getContext().getResources()
                            .getString(R.string.trip_already_departed);
                } else {

                    statusText = getContext().getResources()
                            .getString(R.string.trip_already_arrived);
                }

                statusTextView.setText(statusText);

                timeTextView.setVisibility(GONE);
                minuteTextView.setVisibility(GONE);
                statusTextView.setVisibility(VISIBLE);
            }
        }

        // Show track number
        String trackNumber = prediction.getTrackNumber();
        if (prediction.getRoute().getMode() == Route.COMMUTER_RAIL &&
                trackNumber != null && !trackNumber.equals("") && !trackNumber.equals("null")) {
            trackNumber = getContext().getResources().getString(R.string.track) + " " + trackNumber;
            trackNumberTextView.setText(trackNumber);
            trackNumberTextView.setVisibility(VISIBLE);
        } else {
            trackNumberTextView.setVisibility(GONE);
        }

        // Show skipped or cancelled indicators
        if (prediction.getStatus() == Prediction.SKIPPED) {
            cancelledIndicator.setText(R.string.skipped);
            cancelledIndicator.setVisibility(VISIBLE);
            dropOffIndicator.setVisibility(GONE);
            stopIconCancelled.setVisibility(VISIBLE);
            stopIcon.setVisibility(GONE);
            predictionInfoLayout.setVisibility(GONE);

        } else if (prediction.getStatus() == Prediction.CANCELLED) {
            cancelledIndicator.setText(R.string.cancelled);
            cancelledIndicator.setVisibility(VISIBLE);
            dropOffIndicator.setVisibility(GONE);
            stopIconCancelled.setVisibility(VISIBLE);
            stopIcon.setVisibility(GONE);
            predictionInfoLayout.setVisibility(GONE);

        } else if (!prediction.willPickUpPassengers()) {
            cancelledIndicator.setVisibility(GONE);
            dropOffIndicator.setVisibility(VISIBLE);
            stopIconCancelled.setVisibility(GONE);
            stopIcon.setVisibility(VISIBLE);
            predictionInfoLayout.setVisibility(VISIBLE);

        } else {
            cancelledIndicator.setVisibility(GONE);
            dropOffIndicator.setVisibility(GONE);
            stopIconCancelled.setVisibility(GONE);
            stopIcon.setVisibility(VISIBLE);
            predictionInfoLayout.setVisibility(VISIBLE);
        }

        // Show stop name
        stopName.setText(prediction.getStop().getName());

        // Set line colors
        Drawable background = topLineView.getBackground();
        DrawableCompat.setTint(background, Color.parseColor(prediction.getRoute().getPrimaryColor()));
        topLineView.setBackground(background);
        bottomLineView.setBackground(background);

        // Set stop icon color
        Drawable icon = stopIcon.getDrawable();
        DrawableCompat.setTint(icon, Color.parseColor(prediction.getRoute().getPrimaryColor()));
        stopIcon.setBackground(icon);

        Drawable fill = stopIconFill.getDrawable();
        DrawableCompat.setTint(fill, Color.WHITE);
        stopIconFill.setVisibility(VISIBLE);

        // Set line visibility
        if (stopSequenceType == FIRST_STOP) {
            topLineView.setVisibility(INVISIBLE);
            bottomLineView.setVisibility(VISIBLE);

        } else if (stopSequenceType == LAST_STOP) {
            topLineView.setVisibility(VISIBLE);
            bottomLineView.setVisibility(INVISIBLE);
            setBottomBorderVisible();

        } else if (stopSequenceType == ONLY_STOP) {
            topLineView.setVisibility(INVISIBLE);
            bottomLineView.setVisibility(INVISIBLE);

        } else {
            topLineView.setVisibility(VISIBLE);
            bottomLineView.setVisibility(VISIBLE);
        }

        // Set wheelchair accessibility icon visibility
        if (prediction.getStop().isWheelchairAccessible()) {
            wheelchairAccessibleIcon.setVisibility(VISIBLE);
        } else {
            wheelchairAccessibleIcon.setVisibility(INVISIBLE);
        }

        // Set stop alert icon
        List<ServiceAlert> stopAlerts = prediction.getStop().getServiceAlerts();
        if (stopAlerts.size() > 0) {
            for (ServiceAlert alert : stopAlerts) {
                if (alert.isUrgent()) {
                    stopAlertIcon.setVisibility(VISIBLE);
                    break;
                }
            }
            if (stopAlertIcon.getVisibility() != VISIBLE) {
                stopAdvisoryIcon.setVisibility(VISIBLE);
            }
        } else {
            stopAdvisoryIcon.setVisibility(GONE);
            stopAlertIcon.setVisibility(GONE);
        }

        // Set vehicle icon
        if (vehicle != null && vehicle.getTripId().equalsIgnoreCase(prediction.getTripId())) {
            int currentStopSequence = prediction.getStopSequence();
            int nextStopSequence = (nextPrediction != null) ? nextPrediction.getStopSequence() : -1;
            int vehicleStopSequence = vehicle.getCurrentStopSequence();

            if (vehicleStopSequence == currentStopSequence &&
                    prediction.getCountdownTime() <= COUNTDOWN_ARRIVING_CUTOFF) {
                if (prediction.getCountdownTime() > 20000) {
                    vehicleIcons.getChildAt(0).setVisibility(VISIBLE);
                } else {
                    vehicleIcons.getChildAt(1).setVisibility(VISIBLE);
                }
            } else if (vehicleStopSequence > currentStopSequence &&
                    vehicleStopSequence < nextStopSequence) {
                vehicleIcons.getChildAt(2).setVisibility(VISIBLE);

            } else if (vehicleStopSequence == nextStopSequence &&
                    nextPrediction != null &&
                    nextPrediction.getCountdownTime() > COUNTDOWN_ARRIVING_CUTOFF) {
                vehicleIcons.getChildAt(2).setVisibility(VISIBLE);
            }
        }
    }

    public void emphasize() {
        stopName.setTypeface(stopName.getTypeface(), Typeface.BOLD_ITALIC);
        stopIconFillCurrent.setVisibility(VISIBLE);
    }

    public void setBottomBorderVisible() {
        bottomDivider.setVisibility(GONE);
        bottomEdge.setVisibility(VISIBLE);
        bottomBorder.setVisibility(VISIBLE);
    }

    public void clear() {
        topLineView.setVisibility(INVISIBLE);
        bottomLineView.setVisibility(INVISIBLE);
        predictionInfoLayout.setVisibility(VISIBLE);
        stopName.setText("");
        stopName.setTypeface(Typeface.DEFAULT);
        stopIconFillCurrent.setVisibility(GONE);
        stopIconCancelled.setVisibility(GONE);
        wheelchairAccessibleIcon.setVisibility(GONE);
        stopAdvisoryIcon.setVisibility(GONE);
        stopAlertIcon.setVisibility(GONE);
        timeTextView.setText("");
        minuteTextView.setText("");
        statusTextView.setText("");
        statusTextView.setVisibility(GONE);
        trackNumberTextView.setVisibility(GONE);
        cancelledIndicator.setVisibility(GONE);
        dropOffIndicator.setVisibility(GONE);
        bottomDivider.setVisibility(VISIBLE);
        bottomEdge.setVisibility(GONE);
        bottomBorder.setVisibility(GONE);

        for (int i = 0; i < vehicleIcons.getChildCount(); i++) {
            vehicleIcons.getChildAt(i).setVisibility(INVISIBLE);
        }
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.item_trip_detail_prediction, this);
        topLineView = rootView.findViewById(R.id.top_line_view);
        bottomLineView = rootView.findViewById(R.id.bottom_line_view);
        predictionInfoLayout = rootView.findViewById(R.id.prediction_info_layout);
        vehicleIcons = rootView.findViewById(R.id.vehicle_icons);
        stopIcon = rootView.findViewById(R.id.stop_icon);
        stopIconFill = rootView.findViewById(R.id.stop_icon_fill);
        stopIconFillCurrent = rootView.findViewById(R.id.stop_icon_fill_current);
        stopIconCancelled = rootView.findViewById(R.id.stop_icon_cancelled);
        stopName = rootView.findViewById(R.id.stop_name_text_view);
        wheelchairAccessibleIcon = rootView.findViewById(R.id.wheelchair_accessible_icon);
        stopAdvisoryIcon = rootView.findViewById(R.id.stop_advisory_icon);
        stopAlertIcon = rootView.findViewById(R.id.stop_alert_icon);
        timeTextView = rootView.findViewById(R.id.time_text_view);
        minuteTextView = rootView.findViewById(R.id.minute_text_view);
        statusTextView = rootView.findViewById(R.id.status_text_view);
        trackNumberTextView = rootView.findViewById(R.id.track_number_text_view);
        cancelledIndicator = rootView.findViewById(R.id.cancelled_text_view);
        dropOffIndicator = rootView.findViewById(R.id.drop_off_text_view);
        bottomDivider = rootView.findViewById(R.id.bottom_divider);
        bottomEdge = rootView.findViewById(R.id.bottom_edge);
        bottomBorder = rootView.findViewById(R.id.bottom_border);

        min = context.getResources().getString(R.string.min);
    }
}
