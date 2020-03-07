package jackwtat.simplembta.views;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import jackwtat.simplembta.R;
import jackwtat.simplembta.model.Prediction;
import jackwtat.simplembta.model.Vehicle;
import jackwtat.simplembta.model.Route;
import jackwtat.simplembta.utilities.Constants;

public class RouteSearchPredictionItem extends LinearLayout implements Constants {
    View rootView;
    View mainContent;
    TextView timeTextView;
    TextView minuteTextView;
    TextView liveIndicator;
    TextView trackNumberIndicator;
    TextView enRouteIndicator;
    TextView tomorrowIndicator;
    TextView weekDayIndicator;
    TextView cancelledIndicator;
    TextView dropOffIndicator;
    TextView destinationTextView;
    TextView vehicleNumberTextView;
    ImageView enRouteIcon;
    View bottomEdge;
    View bottomBorder;

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
        setTime(prediction);
        setDestination(prediction.getDestination());
        setFutureIndicator(prediction);
        setLiveIndicator(prediction);

        mainContent.setVisibility(VISIBLE);
        bottomEdge.setVisibility(VISIBLE);
        bottomBorder.setVisibility(GONE);
    }

    public void setTime(Prediction prediction) {
        // Departure time
        long countdownTime = prediction.getCountdownTime();
        Vehicle vehicle = prediction.getVehicle();

        // There is a vehicle currently on this trip and trip is not skipped or cancelled
        if (vehicle != null && vehicle.getTripId().equalsIgnoreCase(prediction.getTripId()) &&
                prediction.getStatus() != Prediction.SKIPPED &&
                prediction.getStatus() != Prediction.CANCELLED) {

            // Vehicle has already passed this stop
            if (vehicle.getCurrentStopSequence() > prediction.getStopSequence()) {
                String statusText;
                if (prediction.getPredictionType() == Prediction.DEPARTURE) {
                    statusText = getContext().getResources().getString(R.string.route_departed);
                } else {
                    statusText = getContext().getResources().getString(R.string.route_arrived);
                }

                timeTextView.setText(statusText);
                minuteTextView.setVisibility(GONE);

                // Vehicle is at or approaching this stop
            } else if (vehicle.getCurrentStopSequence() == prediction.getStopSequence() ||
                    countdownTime < COUNTDOWN_APPROACHING_CUTOFF) {

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
                    minuteTextView.setVisibility(VISIBLE);

                    // Vehicle is less than one minute away
                } else {
                    String statusText;

                    if (prediction.getPredictionType() == Prediction.ARRIVAL) {
                        if (countdownTime < COUNTDOWN_ARRIVING_CUTOFF) {
                            if (vehicle.getCurrentStatus() == Vehicle.Status.STOPPED) {
                                statusText = getContext().getResources().getString(R.string.route_departing);
                            } else {
                                statusText = getContext().getResources().getString(R.string.route_arriving);
                            }
                        } else {
                            statusText = getContext().getResources().getString(R.string.route_approaching);
                        }
                    } else {
                        statusText = getContext().getResources().getString(R.string.route_departing);
                    }

                    timeTextView.setText(statusText);
                    minuteTextView.setVisibility(GONE);
                }

                // Vehicle is not yet approaching this stop
            } else {
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
                minuteTextView.setVisibility(VISIBLE);
            }

            // No vehicle is on this trip or trip is skipped or cancelled
        } else {
            String timeText;
            String minuteText;

            if (countdownTime < COUNTDOWN_HOUR_CUTOFF &&
                    prediction.getStatus() != Prediction.SKIPPED &&
                    prediction.getStatus() != Prediction.CANCELLED) {
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

            if (!prediction.isLive()) {
                minuteText += "*";
            }

            if (prediction.getStatus() == Prediction.SKIPPED ||
                    prediction.getStatus() == Prediction.CANCELLED) {
                timeTextView.setPaintFlags(timeTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                minuteTextView.setPaintFlags(minuteTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                timeTextView.setPaintFlags(timeTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                minuteTextView.setPaintFlags(minuteTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            timeTextView.setText(timeText);
            minuteTextView.setText(minuteText);
            minuteTextView.setVisibility(VISIBLE);
        }
    }

    public void setFutureIndicator(Prediction prediction) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(prediction.getPredictionTime());
        int predictionDay = calendar.get(Calendar.DAY_OF_MONTH);
        int predictionWeekday = calendar.get(Calendar.DAY_OF_WEEK);
        String predictionDayName = calendar.getDisplayName(
                Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);

        calendar.setTime(new Date());
        int todayDay = calendar.get(Calendar.DAY_OF_MONTH);
        int todayWeekday = calendar.get(Calendar.DAY_OF_WEEK);

        if (predictionDay - todayDay != 0) {
            if ((predictionWeekday - todayWeekday + 7) % 7 > 1) {
                weekDayIndicator.setText(predictionDayName.toUpperCase());
                weekDayIndicator.setVisibility(VISIBLE);
            } else {
                tomorrowIndicator.setVisibility(VISIBLE);
            }
        }
    }

    public void setLiveIndicator(Prediction prediction) {
        // Show the appropriate status indicators
        if (prediction.getStatus() == Prediction.CANCELLED ||
                prediction.getStatus() == Prediction.SKIPPED) {

            if (prediction.getStatus() == Prediction.CANCELLED) {
                cancelledIndicator.setText(R.string.cancelled);
            } else {
                cancelledIndicator.setText(R.string.skipped);
            }

            cancelledIndicator.setVisibility(VISIBLE);
            liveIndicator.setVisibility(GONE);

        } else if (!prediction.willPickUpPassengers()) {
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

            } else if (prediction.getVehicle() != null &&
                    prediction.getVehicle().getCurrentStopSequence() > 1 &&
                    prediction.getTripId().equalsIgnoreCase(prediction.getVehicle().getTripId())) {
                enRouteIndicator.setVisibility(VISIBLE);
                liveIndicator.setVisibility(GONE);

            } else {
                trackNumberIndicator.setVisibility(GONE);
                enRouteIndicator.setVisibility(GONE);
                liveIndicator.setVisibility(VISIBLE);
            }
        }

        if (liveIndicator.getVisibility() == GONE &&
                trackNumberIndicator.getVisibility() == GONE &&
                enRouteIndicator.getVisibility() == GONE &&
                cancelledIndicator.getVisibility() == GONE &&
                dropOffIndicator.getVisibility() == GONE &&
                tomorrowIndicator.getVisibility() == GONE &&
                weekDayIndicator.getVisibility() == GONE) {
            liveIndicator.setVisibility(INVISIBLE);
        }
    }

    public void setDestination(String destination) {
        destinationTextView.setText(destination);
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

    public void setBottomBorderVisible() {
        bottomBorder.setVisibility(VISIBLE);
    }

    public void clear() {
        timeTextView.setText("");
        minuteTextView.setText("");
        liveIndicator.setVisibility(GONE);
        trackNumberIndicator.setVisibility(GONE);
        enRouteIndicator.setVisibility(GONE);
        tomorrowIndicator.setVisibility(GONE);
        weekDayIndicator.setVisibility(GONE);
        cancelledIndicator.setVisibility(GONE);
        dropOffIndicator.setVisibility(GONE);
        destinationTextView.setText("");
        vehicleNumberTextView.setText("");
        vehicleNumberTextView.setVisibility(GONE);
    }

    private void init(Context context) {
        rootView = inflate(context, R.layout.item_route_search_prediction, this);
        mainContent = rootView.findViewById(R.id.predictions_card_body);
        timeTextView = rootView.findViewById(R.id.time_text_view);
        minuteTextView = rootView.findViewById(R.id.minute_text_view);
        liveIndicator = rootView.findViewById(R.id.live_text_view);
        trackNumberIndicator = rootView.findViewById(R.id.track_number_text_view);
        enRouteIndicator = rootView.findViewById(R.id.en_route_text_view);
        tomorrowIndicator = rootView.findViewById(R.id.tomorrow_text_view);
        weekDayIndicator = rootView.findViewById(R.id.week_day_text_view);
        cancelledIndicator = rootView.findViewById(R.id.cancelled_text_view);
        dropOffIndicator = rootView.findViewById(R.id.drop_off_text_view);
        destinationTextView = rootView.findViewById(R.id.destination_text_view);
        vehicleNumberTextView = rootView.findViewById(R.id.vehicle_number_text_view);
        enRouteIcon = rootView.findViewById(R.id.enroute_icon);
        bottomEdge = rootView.findViewById(R.id.bottom_edge);
        bottomBorder = rootView.findViewById(R.id.bottom_border);

        min = context.getResources().getString(R.string.min);
    }
}
